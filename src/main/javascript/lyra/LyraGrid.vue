<template>
  <div class="lyra-grid">
    <div v-html="header" />
    <div :id="gridDivId" />
    <div v-html="footer" />
  </div>
</template>


<style src="dgrid/css/dgrid.css"></style>
<style src="./css/lyra.css"></style>


<script>

/*
import SockJS from 'sockjs-client'
import Stomp from 'stompjs'
*/
import SockJS from 'sockjs-client/dist/sockjs';
import 'stompjs/lib/stomp';

import Vue from 'vue';
import request from 'dojo/request';
import query from 'dojo/query';
import lang from 'dojo/_base/lang';
import Grid from 'dgrid/OnDemandGrid';
import ColumnResizer from 'dgrid/extensions/ColumnResizer';
import ColumnHider from 'dgrid/extensions/ColumnHider';
import ColumnReorder from 'dgrid/extensions/ColumnReorder';
import Selection from 'dgrid/Selection';
import CellSelection from 'dgrid/CellSelection';
import Keyboard from 'dgrid/Keyboard';
import declare from 'dojo/_base/declare';
import QueryResults from 'dstore/QueryResults';
import Rest from 'dstore/Rest';
import Cache from 'dstore/Cache';
import when from 'dojo/when';
import domConstruct from 'dojo/dom-construct';


const arrGrids = [];


const socket = new SockJS('/lyra/scrollback');
const stompClient = Stomp.over(socket);
stompClient.connect({}, (frame) => {
  stompClient.subscribe('/position', (scrollBackParams) => {
    const params = JSON.parse(scrollBackParams.body);
    const grid = arrGrids[params.dgridId];
    if (grid.needBackScroll) {
      let pos = params.position;
      pos *= grid.rowHeight;
      pos = pos + grid.getScrollPosition().y - Math.floor(grid.getScrollPosition().y / grid.rowHeight) * grid.rowHeight;
      pos = Math.round(pos);
      if (!isNaN(pos) && (pos >= 0)) {
        grid.backScroll = true;
        grid.scrollTo({ x: 0, y: pos });
      }
    }
  });
});


export const lyraGridEvents = new Vue();
lyraGridEvents.$on('refresh', (formClass, instanceId, context) => {
  refreshLyraVueDGrid(getParentId(formClass, instanceId), context);
});
lyraGridEvents.$on('export-to-clipboard', (formClass, instanceId) => {
  exportToClipboardLyraVueDGrid(getParentId(formClass, instanceId));
});
lyraGridEvents.$on('export-to-excel', (formClass, instanceId, exportType, fileName) => {
  exportToExcelLyraVueDGrid(getParentId(formClass, instanceId), exportType, fileName);
});
lyraGridEvents.$on('file-download', (formClass, instanceId, procName) => {
  fileDownloadLyraVueDGrid(getParentId(formClass, instanceId), procName);
});
lyraGridEvents.$on('set-columns-visibility', (formClass, instanceId, columns) => {
  setColumnsVisibility(getParentId(formClass, instanceId), columns);
});


export default {
  name: 'LyraGrid',

  props: ['formclass', 'instanceid', 'context'],

  data() {
    return {
      gridDivId: getParentId(this.formclass, this.instanceid).replace(/\./g, '-'),

      header: '',
      footer: '',
    };
  },

  mounted() {
    const { gridDivId } = this;
    const parentId = getParentId(this.formclass, this.instanceid);

    const vueComponent = this;

    dojo.xhrPost({
      url: 'lyra/metadata',

      postData: {
        formClass: this.formclass,
        instanceId: this.instanceid,

        context: this.context,
      },
      handleAs: 'json',
      preventCache: true,
      load(metadata) {
        const div = document.getElementById(gridDivId);
        div.style = `width:${metadata.common.gridWidth}; height:${metadata.common.gridHeight};`;

        createLyraVueDGrid(vueComponent, parentId, div.id, metadata, this.postData.formClass, this.postData.instanceId, this.postData.context);
      },
      error(error) {
        showErrorTextMessage(error.message);
      },
    });
  },

};


function createLyraVueDGrid(vueComponent, parentId, gridDivId, metadata, formClass, instanceId, context) {
  try {
    const columns = [];
    for (const k in metadata.columns) {
      const column = {};

      column.id = metadata.columns[k].id;
      column.parentId = metadata.columns[k].parentId;
      column.field = metadata.columns[k].id;
      column.hidden = !metadata.columns[k].visible;
      // column["unhidable"] = true;
      column.label = metadata.columns[k].caption;
      column.sortingAvailable = metadata.columns[k].sortingAvailable;
      column.className = metadata.columns[k].cssClassName;

      function getTitle(title) {
        let res = title;
        if (res) {
          res = res.replace(/&lt;/g, '<');
          res = res.replace(/&gt;/g, '>');
          res = res.replace(/&amp;/g, '&');
        }
        return res;
      }

      column.renderCell = function actionRenderCell(object, value, node, options) {
        if (!value) {
          value = '';
        }

        const div = document.createElement('div');
        div.innerHTML = value;
        div.title = value;
        div.title = getTitle(div.title);

        return div;
      };


      column.renderHeaderCell = function actionRenderCell(node) {
        const div = document.createElement('div');
        if (metadata.common.haColumnHeader) {
          div.style['text-align'] = metadata.common.haColumnHeader;
        }
        div.innerHTML = this.label;
        div.title = this.label;

        div.title = getTitle(div.title);

        if (this.sortingPic || this.sortingAvailable) {
          div.innerHTML = '<tbody>'
                            + '<tr>';

          div.innerHTML = `${div.innerHTML
          }<td>${this.label}</td>`;

          if (this.sortingPic) {
            div.innerHTML = `${div.innerHTML
            }<td><span class='sort-gap before-sorted'> </span></td>`

                                + '<td align=\'right\' style=\'vertical-align: middle;\'>'
                                + '<a title=\'Порядок и направление сортировки\'>'
                                + `<img src class='${this.sortingPic} sorted-image'>`
                                + '</a>'
                                + '</td>';
          }

          if (this.sortingAvailable) {
            div.innerHTML = `${div.innerHTML
            }<td><span class='sort-gap before-sortable'> </span></td>`

                                + '<td align=\'right\' style=\'vertical-align: middle;\'>'
                                + '<a title=\'По данному полю есть индекс одиночной сортировки\'>'
                                + '<img src class=\'one sortable-image\'>'
                                + '</a>'
                                + '</td>';
          }


          div.innerHTML = `${div.innerHTML
          }</tr>`
                            + '</tbody>';
        }

        return div;
      };


      columns.push(column);
    }
    lyraGridEvents.$emit('columns-info', formClass, instanceId, columns);
    vueComponent.$emit('columns-info', formClass, instanceId, columns);


    const sort = getParamFromContext(context, 'sort');
    setExternalSorting(columns, sort);


    const declareGrid = [Grid, ColumnResizer, ColumnHider, ColumnReorder, Keyboard];

    let selectionMode;
    if (metadata.common.selectionModel === 'RECORDS') {
      selectionMode = 'extended';
      declareGrid.push(Selection);
    } else {
      selectionMode = 'single';
      declareGrid.push(CellSelection);
    }

    let isVisibleColumnsHeader = false;
    if (metadata.common.isVisibleColumnsHeader) {
      isVisibleColumnsHeader = true;
    }

    let isAllowTextSelection = false;
    if (metadata.common.isAllowTextSelection) {
      isAllowTextSelection = true;
    }


    const localizedParams = {
      loadingMessage: 'Загрузка...',
      noDataMessage: 'Нет записей',
    };


    const grid = new declare(declareGrid)({
      columns,

      minRowsPerPage: parseInt(metadata.common.limit),
      maxRowsPerPage: parseInt(metadata.common.limit),
      bufferRows: 0,
      farOffRemoval: 0,
      pagingDelay: 50,

      selectionMode,
      allowTextSelection: isAllowTextSelection,
      showHeader: isVisibleColumnsHeader,
      loadingMessage: localizedParams.loadingMessage,
      noDataMessage: localizedParams.noDataMessage,

      deselectOnRefresh: false,

      keepScrollPosition: false,

      renderRow(object) {
        const rowElement = Grid.prototype.renderRow.call(this, object);
        if (object.rowstyle && (object.rowstyle !== '')) {
          rowElement.className = `${rowElement.className} ${object.rowstyle} `;
        }
        return rowElement;
      },

      backScroll: false,
      resScroll: null,

      needBackScroll: true,


      dgridOldPosition: 0,
      limit: parseInt(metadata.common.limit),

      context,

      firstLoading: true,

      oldSort: 'D13k82F9g7',
      oldFilter: 'D13k82F9g7',

      formClass,
      instanceId,


      showFooter: metadata.common.summaryRow,
      summary: metadata.common.summaryRow ? JSON.parse(metadata.common.summaryRow) : null,

      buildRendering() {
        ColumnResizer.prototype.buildRendering.call(this);
        const areaNode = this.summaryAreaNode = domConstruct.create('div', {
          className: 'summary-row',
          role: 'row',
          style: { overflow: 'hidden' },
        }, this.footerNode);

        this.on('scroll', lang.hitch(this, function () {
          areaNode.scrollLeft = this.getScrollPosition().x;
        }));
      },

      _updateColumns() {
        Grid.prototype._updateColumns.call(this);
        if (this.summary) {
          this._setSummary(this.summary);
        }
      },

      _renderSummaryCell(item, cell, column) {
        const value = item[column.field] || '';
        cell.appendChild(document.createTextNode(value));
      },

      _setSummary(data) {
        let tableNode = this.summaryTableNode;

        this.summary = data;

        if (tableNode) {
          domConstruct.destroy(tableNode);
        }

        tableNode = this.summaryTableNode = this.createRowCells('td',
          lang.hitch(this, '_renderSummaryCell', data));
        this.summaryAreaNode.appendChild(tableNode);

        if (this._started) {
          this.resize();
        }
      },

      _adjustFooterCellsWidths() {
        if (!this._resizedColumns) {
          const colNodes = query('.dgrid-cell', this.headerNode);

          const colWidths = colNodes.map(colNode => colNode.offsetWidth);

          colNodes.forEach(function (colNode, i) {
            this.resizeColumnWidth(colNode.columnId, colWidths[i]);
          }, this);
        }

        const obj = this._getResizedColumnWidths();
        const lastCol = obj.lastColId;

        this.resizeColumnWidth(lastCol, 'auto');
      },


    }, gridDivId);
    arrGrids[parentId] = grid;


    const store = new declare([Rest, Cache])(lang.mixin({

      target: 'lyra/data',
      idProperty: 'internalId',

      grid,


      _fetchRange(scparams) {
        const headers = lang.delegate(this.headers, { Accept: this.accepts });

        const response = request(this.target, {
          method: 'POST',
          data: scparams,
          headers,
        });

        const collection = this;
        const parsedResponse = response.then(response => collection.parse(response));
        const results = {
          data: parsedResponse.then((data) => {
            const results = data.items || data;
            for (let i = 0, l = results.length; i < l; i++) {
              results[i] = collection._restore(results[i], true);
            }
            return results;
          }),
          total: parsedResponse.then((data) => {
            const { total } = data;
            if (total > -1) {
              return total;
            }
            return response.response.then((response) => {
              let range = response.getHeader('Content-Range');
              return range && (range = range.match(/\/(.*)/)) && +range[1];
            });
          }),
          response: response.response,
        };

        return new QueryResults(results.data, {
          totalLength: results.total,
          response: results.response,
        });
      },


      _fetch(kwArgs) {
        let results = null;

        if (this.grid.backScroll) {
          results = new QueryResults(when(this.grid.resScroll), {
            totalLength: when(this.grid._total),
          });

          setTimeout(() => {
            arrGrids[parentId].backScroll = false;
          }, 150);

          return results;
        }

        this.grid.needBackScroll = true;

        let refreshId = null;
        if (this.grid.refreshId) {
          refreshId = this.grid.refreshId;

          if (this.grid.oldStart > 0) {
            this.grid.oldStart = 0;

            results = new QueryResults(when(this.grid.resScroll), {
              totalLength: when(this.grid._total),
            });
            return results;
          }
        }

        this.grid.oldStart = kwArgs[0].start;

        const scparams = {};
        scparams.context = this.grid.context;
        scparams.offset = kwArgs[0].start;
        scparams.limit = kwArgs[0].end - kwArgs[0].start;
        scparams.dgridOldPosition = this.grid.dgridOldPosition;
        this.grid.dgridOldPosition = scparams.offset;
        this.grid.limit = scparams.limit;

        const sort = getParamFromContext(this.grid.context, 'sort');
        const filter = getParamFromContext(this.grid.context, 'filter');
        if ((sort !== this.grid.oldSort) || (filter !== this.grid.oldFilter)) {
          scparams.sortingOrFilteringChanged = true;
          scparams.dgridOldPosition = 0;
          this.grid.dgridOldPosition = 0;
          this.grid.oldSort = sort;
          this.grid.oldFilter = filter;
        }

        scparams.firstLoading = this.grid.firstLoading;
        scparams.refreshId = refreshId;
        scparams.formClass = formClass;
        scparams.instanceId = instanceId;


        results = this._fetchRange(scparams);
        results.then((results) => {
          let addData = null;

          if (results && (!results[0]) && results.internalAddData) {
            addData = results.internalAddData;
          }

          if (results[0]) {
            const grid = arrGrids[parentId];
            grid.resScroll = results;

            if (results[0].internalAddData) {
              addData = results[0].internalAddData;
            }

            if (results[0].dgridNewPosition) {
              grid.dgridNewPosition = results[0].dgridNewPosition;
              grid.dgridNewPositionId = results[0].dgridNewPositionId;

              grid.dgridOldPosition = grid.dgridNewPosition;
            }
          }

          if (addData) {
            vueComponent.header = addData.header;
            vueComponent.footer = addData.footer;
          }
        }, (err) => {
          showErrorTextMessage(err.response.text);
        });

        return results;
      },
    }, {}));
    grid.set('collection', store);


    for (const k in metadata.columns) {
      grid.styleColumn(metadata.columns[k].id, metadata.columns[k].cssStyle);
    }

    if (grid.summary) {
      grid._setSummary(grid.summary);
      grid._adjustFooterCellsWidths();
    }


    grid.on('dgrid-columnreorder', (event) => {
      setTimeout(() => {
        if (event.grid.summary) {
          event.grid._adjustFooterCellsWidths();
        }
      });
    });

    grid.on('dgrid-select', (event) => {
      if (event.parentType && ((event.parentType.indexOf('mouse') > -1) || (event.parentType.indexOf('pointer') > -1))) {
        return;
      }

      emitSelect(grid.row(event.grid._focusedNode));
    });
    grid.on('.dgrid-row:click', (event) => {
      if (grid.row(event) && grid.column(event)) {
        emitSelect(grid.row(event));
      }
    });

    function emitSelect(row) {
      const obj = {};
      obj.currentRowId = row.id;
      obj.currentRowData = row.data;
      obj.selection = getSelection(grid);

      lyraGridEvents.$emit('select', grid.formClass, grid.instanceId, obj);
      vueComponent.$emit('select', grid.formClass, grid.instanceId, obj);
    }

    grid.on('.dgrid-row:dblclick', (event) => {
      if (grid.row(event) && grid.column(event)) {
        obj = {};
        obj.currentRowId = grid.row(event).id;
        obj.currentRowData = grid.row(event).data;
        obj.selection = getSelection(grid);

        lyraGridEvents.$emit('dblclick', grid.formClass, grid.instanceId, obj);
        vueComponent.$emit('dblclick', grid.formClass, grid.instanceId, obj);
      }
    });


    grid.on('dgrid-sort', (event) => {
      let sort = event.sort[0].property;
      if (event.sort[0].descending) {
        sort = `${sort} desc`;
      }

      const primaryKey = metadata.common.primaryKey.split(',');
      for (let n = 0; n < primaryKey.length; n++) {
        if ((n === 0) && (event.sort[0].property === primaryKey[n])) {
          continue;
        }
        sort = `${sort},${primaryKey[n]}`;
        if (event.sort[0].descending) {
          sort = `${sort} desc`;
        }
      }


      const { context } = event.grid;

      const refreshParams = {
        selectKey: '',
        sort,
        filter: '',
      };

      let objContext;
      if (!context || context.trim() === '') {
        objContext = { refreshParams };
      } else {
        objContext = JSON.parse(context);
        if (objContext.refreshParams) {
          objContext.refreshParams.selectKey = '';
          objContext.refreshParams.sort = sort;
        } else {
          objContext.refreshParams = refreshParams;
        }
      }

      event.grid.context = JSON.stringify(objContext);

      setExternalSorting(event.grid._columns, sort);
      event.grid.renderHeader();

      event.grid.firstLoading = true;
    });


    grid.on('dgrid-refresh-complete', (event) => {
      event.grid.refreshId = null;

      if (event.grid.firstLoading) {
        if (event.grid.dgridNewPosition) {
          let pos = parseInt(event.grid.dgridNewPosition);
          pos *= event.grid.rowHeight;
          event.grid.backScroll = true;
          event.grid.needBackScroll = false;
          event.grid.scrollTo({ x: 0, y: pos });
          event.grid.select(event.grid.row(event.grid.dgridNewPositionId));
          event.grid.row(event.grid.dgridNewPositionId).element.scrollIntoView({
            block: 'start',
            behavior: 'smooth',
          });
          event.grid.dgridNewPosition = null;
          event.grid.dgridNewPositionId = null;
        }


        if (metadata.common.selectionModel === 'RECORDS') {
          if (metadata.common.selRecId) {
            event.grid.select(event.grid.row(metadata.common.selRecId));
          }
        } else if (metadata.common.selRecId && metadata.common.selColId) {
          for (const col in event.grid.columns) {
            if (event.grid.columns[col].label === metadata.common.selColId) {
              event.grid.select(event.grid.cell(metadata.common.selRecId, col));
              break;
            }
          }
        }
        event.grid.firstLoading = false;
      }
    });

    if (columns[0]) {
      grid.resizeColumnWidth(columns[0].field, '5px');
    }
  } catch (err) {
    showErrorTextMessage(err);
    throw err;
  }
}


function getParentId(formClass, instanceId) {
  return `${formClass}.${instanceId}`;
}

function getSelection(grid) {
  const selection = [];
  for (const id in grid.selection) {
    if (grid.selection[id]) {
      selection.push(id);
    }
  }
  return selection;
}

function setExternalSorting(columns, sort) {
  if (!sort) {
    return;
  }

  for (let n = 0; n < columns.length; n++) {
    columns[n].sortingPic = null;
  }

  let desc = false;
  const arr = sort.split(',');
  for (let m = 0; m < arr.length; m++) {
    if ((m === 0) && (arr[m].toLowerCase().indexOf(' desc') > -1)) {
      desc = true;
    }

    let sortName = arr[m].substring(0, arr[m].indexOf(' '));
    if (sortName === '') {
      sortName = arr[m];
    }

    for (let n = 0; n < columns.length; n++) {
      if (columns[n].id === sortName) {
        if (desc) {
          columns[n].sortingPic = 'd';
        } else {
          columns[n].sortingPic = 'a';
        }

        columns[n].sortingPic = columns[n].sortingPic + (m + 1);

        break;
      }
    }
  }
}


function getParamFromContext(context, param) {
  let ret = '';
  if (context && context.trim() !== '') {
    const objContext = JSON.parse(context);
    if (objContext && objContext.refreshParams && objContext.refreshParams[param]) {
      ret = objContext.refreshParams[param];
    }
  }
  return ret;
}

function refreshLyraVueDGrid(parentId, context) {
  const grid = arrGrids[parentId];
  if (getParamFromContext(context, 'selectKey') === 'current') {
    const focusedNode = grid._focusedNode || grid.contentNode;
    const row = grid.row(focusedNode);

    const sort = getParamFromContext(context, 'sort');
    const filter = getParamFromContext(context, 'filter');
    if ((sort === grid.oldSort) && (filter === grid.oldFilter)) {
      if (context && context.trim() !== '') {
        grid.context = context;
      }

      grid.refreshId = grid.row(row).id;

      grid.firstLoading = false;
      grid.refresh({ keepScrollPosition: true });
    } else {
      const selectKey = getSelection(grid)[0];

      if (!selectKey) {
        showErrorTextMessage('Отсутствует выделенная запись. Выполнение операции невозможно.');
        return;
      }

      const refreshParams = {
        selectKey,
        sort: '',
        filter: '',
      };

      let objContext;
      if (!context || context.trim() === '') {
        objContext = { refreshParams };
      } else {
        objContext = JSON.parse(context);
        if (objContext.refreshParams) {
          objContext.refreshParams.selectKey = selectKey;
        } else {
          objContext.refreshParams = refreshParams;
        }
      }

      grid.context = JSON.stringify(objContext);

      setExternalSorting(grid._columns, sort);
      grid.renderHeader();

      grid.firstLoading = true;
      grid.refresh({ keepScrollPosition: false });
    }
  } else {
    if (context && context.trim() !== '') {
      grid.context = context;
    }

    const sort = getParamFromContext(grid.context, 'sort');
    if (sort && sort.trim() !== '') {
      setExternalSorting(grid._columns, sort);
      grid.renderHeader();
    }

    grid.firstLoading = true;
    grid.refresh({ keepScrollPosition: false });
  }
}

function exportToClipboardLyraVueDGrid(parentId) {
  let str = '';

  const grid = grid;

  for (const col in grid.columns) {
    str = `${str + grid.columns[col].label}\t`;
  }

  str = `${str}\n`;

  for (const id in grid.selection) {
    if (grid.selection[id]) {
      for (const col in grid.columns) {
        str = `${str + grid.row(id).data[col]}\t`;
      }
      str = `${str}\n`;
    }
  }

  // gwtLyraVueGridExportToClipboard(str);
}

function exportToExcelLyraVueDGrid(parentId, exportType, fileName) {
  const grid = arrGrids[parentId];
  const focusedNode = grid._focusedNode || grid.contentNode;
  const row = grid.row(focusedNode);
  const refreshId = grid.row(row).id;

  /*
                gwtLyraVueGridExportToExcel(
                    grid.formClass,
                    grid.instanceId,
                    grid.context,
                    refreshId,
                    grid.dgridOldPosition,
                    grid.limit,
                    exportType,
                    fileName);
        */
}

function fileDownloadLyraVueDGrid(parentId, procName) {
  const grid = arrGrids[parentId];
  const recId = getSelection(grid)[0];

  /*
                gwtProcessFileDownloadLyraVue(
                    grid.formClass,
                    grid.instanceId,
                    encodeURIComponent(grid.context),
                    recId,
                    procName,
                    "false");
        */
}

function setColumnsVisibility(parentId, columns) {
  const grid = arrGrids[parentId];
  for (let n = 0; n < columns.length; n++) {
    grid.toggleColumnHiddenState(columns[n].id, !columns[n].visible);
  }
}


function showErrorTextMessage(message) {
  alert(`Ошибка: ${message}`);
}


</script>
