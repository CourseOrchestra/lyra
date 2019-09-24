import request from 'dojo/request';
import lang from 'dojo/_base/lang';
import Declare from 'dojo/_base/declare';
import QueryResults from 'dojo-dstore/QueryResults';
import Rest from 'dojo-dstore/Rest';
import Cache from 'dojo-dstore/Cache';
import when from 'dojo/when';

import { getDataUrl, isEqual } from './util';


export default function createStore(aGrid, showMessage, setLabels) {
  const grid = aGrid;

  const store = new Declare([Rest, Cache])(lang.mixin({

    target: getDataUrl(),
    idProperty: 'internalId',

    fetchRange2(scparams) {
      const headers = lang.delegate({
        'Content-Type': 'application/json',
        'X-Requested-With': null,
      }, { Accept: this.accepts });

      const response = request(this.target, {
        method: 'POST',
        headers,
        data: JSON.stringify(scparams),
      });

      const collection = this;
      const parsedResponse = response.then((resp) => collection.parse(resp));
      const results = {
        data: parsedResponse.then((data) => {
          const res = data.items || data;
          for (let i = 0, l = res.length; i < l; i += 1) {
            res[i] = collection._restore(res[i], true);
          }
          return res;
        }),
        total: parsedResponse.then(() => response.response.then((resp) => {
          let range = resp.getHeader('Content-Range');
          range = range && (range = range.match(/\/(.*)/)) && +range[1];
          return range;
        })),
      };

      return results;
    },


    _fetch(kwArgs) {
      let results = null;

      if (grid.backScroll) {
        results = new QueryResults(when(grid.resScroll), {
          totalLength: when(grid._total),
        });

        setTimeout(() => {
          grid.backScroll = false;
        }, 150);

        return results;
      }

      grid.needBackScroll = true;

      if (grid.refreshId && (grid.oldStart > 0)) {
        grid.oldStart = 0;

        results = new QueryResults(when(grid.resScroll), {
          totalLength: when(grid._total),
        });
        return results;
      }

      grid.oldStart = kwArgs[0].start;


      const formInstantiationParams = {
        clientParams: grid.context,
        formClass: grid.formClass,
        instanceId: grid.instanceId,
      };

      const dataRetrievalParams = {
        offset: kwArgs[0].start,
        limit: kwArgs[0].end - kwArgs[0].start,
        dgridOldPosition: grid.dgridOldPosition,

        selectKey: grid.context.refreshParams.selectKey,
        firstLoading: grid.firstLoading,
        refreshId: grid.refreshId,

      };
      grid.dgridOldPosition = dataRetrievalParams.offset;
      grid.limit = dataRetrievalParams.limit;

      const { sort } = grid.context.refreshParams;
      const { filter } = grid.context.refreshParams;
      if (!isEqual(sort, grid.oldSort) || !isEqual(filter, grid.oldFilter)) {
        dataRetrievalParams.sortingOrFilteringChanged = true;
        dataRetrievalParams.dgridOldPosition = 0;
        grid.dgridOldPosition = 0;
        grid.oldSort = sort;
        grid.oldFilter = filter;
      }


      const fetchRangeResults = this.fetchRange2({
        formInstantiationParams,
        dataRetrievalParams,
      });
      results = QueryResults(fetchRangeResults.data, {
        totalLength: fetchRangeResults.total,
      });
      results.then((res) => {
        let addData = null;

        if (res && (!res[0]) && res.internalAddData) {
          addData = res.internalAddData;
        }

        if (res[0]) {
          grid.resScroll = res;

          if (res[0].internalAddData) {
            addData = res[0].internalAddData;
          }

          if (res[0].dgridNewPosition) {
            grid.dgridNewPosition = res[0].dgridNewPosition;
            grid.dgridNewPositionId = res[0].dgridNewPositionId.toString();

            grid.dgridOldPosition = grid.dgridNewPosition;
          }
        }

        if (addData) {
          setLabels(addData.header, addData.footer);
        }
      }, (err) => {
        showMessage(err.response.text);
      });

      return results;
    },
  }, {}));

  return store;
}
