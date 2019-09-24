import {
  mockData,
  mockDataEmpty,
  mockDataNewPosition,
  mockDataNoAddData,
  mockResScroll,
} from './const';
import createStore from '../../src/store';


describe('store.js', () => {
  describe('create store', () => {
    it('check store properties', () => {
      const store = createStore({}, null, null);
      expect(store.target)
        .to
        .equal('/lyra/data');
      expect(store.idProperty)
        .to
        .equal('internalId');
    });
  });

  describe('fetchRange2(scparams)', () => {
    let scparams = null;
    let results = null;
    let req = null;
    before(() => {
      sinon.useFakeXMLHttpRequest().onCreate = function onCreate(xhr) {
        req = xhr;
      };

      const store = createStore({}, null, null);

      scparams = {
        formInstantiationParams: {
          clientParams: {
            part1: 'part1',
            part2: 'part2',
            refreshParams:
              {
                filter: {
                  filter: 'filter conditions',
                },
                sort: ['name', 'code'],
              },
          },
          formClass: 'ru.curs.demo.P1MainLyra',
          instanceId: 'grid1',
        },
        dataRetrievalParams: {
          dgridOldPosition: 0,
          firstLoading: true,
          limit: 50,
          offset: 0,
          sortingOrFilteringChanged: true,
        },
      };

      results = store.fetchRange2(scparams);

      req.respond(200,
        {
          'Content-Type': 'application/json',
          'Content-Range': 'items 0-49/20000',
        },
        JSON.stringify(mockData));
    });
    after(() => {
      sinon.useFakeXMLHttpRequest()
        .restore();
    });

    it('check request headers', () => {
      expect(req.requestHeaders.Accept)
        .to
        .equal('application/json');
      expect(req.requestHeaders['Content-Type'])
        .to
        .have
        .string('application/json');
      expect(req.requestHeaders['X-Requested-With']).to.be.undefined;
    });

    it('check request params', () => {
      expect(JSON.parse(req.requestBody))
        .to
        .deep
        .equal(scparams);
    });

    it('check results, total', (done) => {
      results.total.then((total) => {
        expect(total)
          .to
          .equal(20000);
        done();
      });
    });

    it('check results, data', (done) => {
      results.data.then((data) => {
        expect(data.length)
          .to
          .equal(50);
        expect(data[0].code)
          .to
          .equal('63028000006005400');
        expect(data[0].internalId)
          .to
          .deep
          .equal(['63028000006005400']);
        expect(data[0].internalAddData)
          .to
          .deep
          .equal({
            header: '<h5>Eto heder lira-grida</h5>',
            footer: '<h5>refreshParams: {sort=[name, code], filter={filter=filter conditions}}</h5>',
          });
        done();
      });
    });
  });


  describe('_fetch(kwArgs)', () => {
    let req;
    beforeEach(() => {
      req = null;
      sinon.useFakeXMLHttpRequest().onCreate = function onCreate(xhr) {
        req = xhr;
      };
    });
    afterEach(() => {
      sinon.useFakeXMLHttpRequest()
        .restore();
    });


    it('backScroll, request is not fired', (done) => {
      const grid = {
        backScroll: true,
        resScroll: mockResScroll,
        _total: 20000,
      };
      const store = createStore(grid, null, null);
      store._fetch(null);

      expect(req).to.be.null;
      done();
    });

    it('backScroll, check grid properties', (done) => {
      const grid = {
        backScroll: true,
        resScroll: mockResScroll,
        _total: 20000,
      };
      const store = createStore(grid, null, null);
      store._fetch(null);

      setTimeout(() => {
        expect(grid.backScroll).to.be.false;
        done();
      }, 200);
    });

    it('backScroll, check QueryResults, totalLength', (done) => {
      const grid = {
        backScroll: true,
        resScroll: mockResScroll,
        _total: 20000,
      };
      const store = createStore(grid, null, null);
      const results = store._fetch(null);

      results.totalLength.then((totalLength) => {
        expect(totalLength)
          .to
          .equal(20000);
        done();
      });
    });

    it('backScroll, check QueryResults, data', (done) => {
      const grid = {
        backScroll: true,
        resScroll: mockResScroll,
        _total: 20000,
      };
      const store = createStore(grid, null, null);
      const results = store._fetch(null);

      results.then((data) => {
        expect(data.length)
          .to
          .equal(50);
        expect(data[0].code)
          .to
          .equal('38000004016004500');
        expect(data[0].internalId)
          .to
          .deep
          .equal(['38000004016004500']);
        expect(data[0].internalAddData)
          .to
          .deep
          .equal({
            header: '<h5>Eto heder lira-grida</h5>',
            footer: '<h5>refreshParams: {sort=[name, code], filter={filter=filter conditions2}}</h5>',
          });
        done();
      });
    });

    it('refreshId, request is not fired', (done) => {
      const grid = {
        backScroll: false,
        resScroll: mockResScroll,
        _total: 20000,
        refreshId: ['38000004016004500'],
        oldStart: 500,
        needBackScroll: false,
      };
      const store = createStore(grid, null, null);
      store._fetch(null);

      expect(req).to.be.null;
      done();
    });

    it('refreshId, check grid properties', (done) => {
      const grid = {
        backScroll: false,
        resScroll: mockResScroll,
        _total: 20000,
        refreshId: ['38000004016004500'],
        oldStart: 500,
        needBackScroll: false,
      };
      const store = createStore(grid, null, null);
      store._fetch(null);

      expect(grid.needBackScroll).to.be.true;
      expect(grid.oldStart)
        .to
        .equal(0);
      done();
    });

    it('refreshId, check QueryResults, totalLength', (done) => {
      const grid = {
        backScroll: false,
        resScroll: mockResScroll,
        _total: 20000,
        refreshId: ['38000004016004500'],
        oldStart: 500,
        needBackScroll: false,
      };
      const store = createStore(grid, null, null);
      const results = store._fetch(null);

      results.totalLength.then((totalLength) => {
        expect(totalLength)
          .to
          .equal(20000);
        done();
      });
    });

    it('refreshId, check QueryResults, data', (done) => {
      const grid = {
        backScroll: false,
        resScroll: mockResScroll,
        _total: 20000,
        refreshId: ['38000004016004500'],
        oldStart: 500,
        needBackScroll: false,
      };
      const store = createStore(grid, null, null);
      const results = store._fetch(null);

      results.then((data) => {
        expect(data.length)
          .to
          .equal(50);
        expect(data[0].code)
          .to
          .equal('38000004016004500');
        expect(data[0].internalId)
          .to
          .deep
          .equal(['38000004016004500']);
        expect(data[0].internalAddData)
          .to
          .deep
          .equal({
            header: '<h5>Eto heder lira-grida</h5>',
            footer: '<h5>refreshParams: {sort=[name, code], filter={filter=filter conditions2}}</h5>',
          });
        done();
      });
    });

    it('standard, sort is changed', (done) => {
      const grid = {
        backScroll: false,
        _total: 20000,
        refreshId: null,
        needBackScroll: false,

        formClass: 'ru.curs.demo.P1MainLyra',
        instanceId: 'grid1',
        context: {
          part1: 'part1',
          part2: 'part2',
          refreshParams:
            {
              filter: {
                filter: 'filter conditions',
              },
              sort: ['name', 'code'],
            },
        },

        oldStart: 500,
        dgridOldPosition: 20,
        sortingOrFilteringChanged: false,
        limit: 0,
      };

      function setLabels() {
      }

      const store = createStore(grid, null, setLabels);
      const kwArgs = [{
        start: 250,
        end: 300,
      }];
      store._fetch(kwArgs);

      req.respond(200,
        {
          'Content-Type': 'application/json',
          'Content-Range': 'items 0-49/20000',
        },
        JSON.stringify(mockData));

      expect(grid.needBackScroll).to.be.true;

      expect(grid.oldStart)
        .to
        .equal(250);

      expect(grid.dgridOldPosition)
        .to
        .equal(0);

      expect(grid.limit)
        .to
        .equal(50);

      expect(grid.oldSort)
        .to
        .deep
        .equal(['name', 'code']);

      expect(grid.oldFilter)
        .to
        .deep
        .equal({
          filter: 'filter conditions',
        });

      done();
    });

    it('standard, filter is changed', (done) => {
      const grid = {
        backScroll: false,
        _total: 20000,
        refreshId: null,
        needBackScroll: false,

        formClass: 'ru.curs.demo.P1MainLyra',
        instanceId: 'grid1',
        context: {
          part1: 'part1',
          part2: 'part2',
          refreshParams:
            {
              filter: {
                filter: 'filter conditions',
              },
              sort: ['name', 'code'],
            },
        },
        oldSort: ['name', 'code'],

        oldStart: 500,
        dgridOldPosition: 20,
        sortingOrFilteringChanged: false,
        limit: 0,
      };

      function setLabels() {
      }

      const store = createStore(grid, null, setLabels);
      const kwArgs = [{
        start: 250,
        end: 300,
      }];
      store._fetch(kwArgs);

      req.respond(200,
        {
          'Content-Type': 'application/json',
          'Content-Range': 'items 0-49/20000',
        },
        JSON.stringify(mockData));

      expect(grid.needBackScroll).to.be.true;

      expect(grid.oldStart)
        .to
        .equal(250);

      expect(grid.dgridOldPosition)
        .to
        .equal(0);

      expect(grid.limit)
        .to
        .equal(50);

      expect(grid.oldSort)
        .to
        .deep
        .equal(['name', 'code']);

      expect(grid.oldFilter)
        .to
        .deep
        .equal({
          filter: 'filter conditions',
        });

      done();
    });

    it('standard, sort is not changed, filter is not changed', (done) => {
      const grid = {
        backScroll: false,
        _total: 20000,
        refreshId: null,
        needBackScroll: false,

        formClass: 'ru.curs.demo.P1MainLyra',
        instanceId: 'grid1',
        context: {
          part1: 'part1',
          part2: 'part2',
          refreshParams:
            {
              filter: {
                filter: 'filter conditions',
              },
              sort: ['name', 'code'],
            },
        },
        oldSort: ['name', 'code'],
        oldFilter: {
          filter: 'filter conditions',
        },

        oldStart: 500,
        dgridOldPosition: 20,
        sortingOrFilteringChanged: false,
        limit: 0,
      };

      function setLabels() {
      }

      const store = createStore(grid, null, setLabels);
      const kwArgs = [{
        start: 250,
        end: 300,
      }];
      store._fetch(kwArgs);

      req.respond(200,
        {
          'Content-Type': 'application/json',
          'Content-Range': 'items 0-49/20000',
        },
        JSON.stringify(mockData));

      expect(grid.needBackScroll).to.be.true;

      expect(grid.oldStart)
        .to
        .equal(250);

      expect(grid.dgridOldPosition)
        .to
        .equal(250);

      expect(grid.limit)
        .to
        .equal(50);

      expect(grid.oldSort)
        .to
        .deep
        .equal(['name', 'code']);

      expect(grid.oldFilter)
        .to
        .deep
        .equal({
          filter: 'filter conditions',
        });

      done();
    });

    it('standard, check request params', (done) => {
      const grid = {
        backScroll: false,
        _total: 20000,
        refreshId: null,
        needBackScroll: false,

        formClass: 'ru.curs.demo.P1MainLyra',
        instanceId: 'grid1',
        context: {
          part1: 'part1',
          part2: 'part2',
          refreshParams:
            {
              filter: {
                filter: 'filter conditions',
              },
              sort: ['name', 'code'],
            },
        },
        oldSort: ['name', 'code'],
        oldFilter: {
          filter: 'filter conditions',
        },

        oldStart: 500,
        dgridOldPosition: 20,
        sortingOrFilteringChanged: false,
        limit: 0,

        firstLoading: false,
      };

      function setLabels() {
      }

      const store = createStore(grid, null, setLabels);
      const kwArgs = [{
        start: 250,
        end: 300,
      }];
      store._fetch(kwArgs);

      req.respond(200,
        {
          'Content-Type': 'application/json',
          'Content-Range': 'items 0-49/20000',
        },
        JSON.stringify(mockData));

      expect(JSON.parse(req.requestBody))
        .to
        .deep
        .equal({
          formInstantiationParams: {
            clientParams: {
              part1: 'part1',
              part2: 'part2',
              refreshParams:
                {
                  filter: {
                    filter: 'filter conditions',
                  },
                  sort: ['name', 'code'],
                },
            },
            formClass: 'ru.curs.demo.P1MainLyra',
            instanceId: 'grid1',
          },
          dataRetrievalParams: {
            dgridOldPosition: 20,
            firstLoading: false,
            limit: 50,
            offset: 250,
            refreshId: null,
          },
        });

      done();
    });

    it('standard, check QueryResults, totalLength', (done) => {
      const grid = {
        backScroll: false,
        _total: 20000,
        refreshId: null,
        needBackScroll: false,

        formClass: 'ru.curs.demo.P1MainLyra',
        instanceId: 'grid1',
        context: {
          part1: 'part1',
          part2: 'part2',
          refreshParams:
            {
              filter: {
                filter: 'filter conditions',
              },
              sort: ['name', 'code'],
            },
        },
        oldSort: ['name', 'code'],
        oldFilter: {
          filter: 'filter conditions',
        },

        oldStart: 500,
        dgridOldPosition: 20,
        sortingOrFilteringChanged: false,
        limit: 0,

        firstLoading: false,
      };

      function setLabels() {
      }

      const store = createStore(grid, null, setLabels);
      const kwArgs = [{
        start: 250,
        end: 300,
      }];
      const results = store._fetch(kwArgs);

      req.respond(200,
        {
          'Content-Type': 'application/json',
          'Content-Range': 'items 0-49/20000',
        },
        JSON.stringify(mockData));

      results.totalLength.then((totalLength) => {
        expect(totalLength)
          .to
          .equal(20000);
        done();
      });
    });


    it('standard, check QueryResults, data', (done) => {
      const grid = {
        backScroll: false,
        _total: 20000,
        refreshId: null,
        needBackScroll: false,

        formClass: 'ru.curs.demo.P1MainLyra',
        instanceId: 'grid1',
        context: {
          part1: 'part1',
          part2: 'part2',
          refreshParams:
            {
              filter: {
                filter: 'filter conditions',
              },
              sort: ['name', 'code'],
            },
        },
        oldSort: ['name', 'code'],
        oldFilter: {
          filter: 'filter conditions',
        },

        oldStart: 500,
        dgridOldPosition: 20,
        sortingOrFilteringChanged: false,
        limit: 0,

        firstLoading: false,
      };

      function setLabels() {
      }

      const store = createStore(grid, null, setLabels);
      const kwArgs = [{
        start: 250,
        end: 300,
      }];
      const results = store._fetch(kwArgs);

      req.respond(200,
        {
          'Content-Type': 'application/json',
          'Content-Range': 'items 0-49/20000',
        },
        JSON.stringify(mockData));

      results.then((data) => {
        expect(data.length)
          .to
          .equal(50);
        expect(data[0].code)
          .to
          .equal('63028000006005400');
        expect(data[0].internalId)
          .to
          .deep
          .equal(['63028000006005400']);
        expect(data[0].internalAddData)
          .to
          .deep
          .equal({
            header: '<h5>Eto heder lira-grida</h5>',
            footer: '<h5>refreshParams: {sort=[name, code], filter={filter=filter conditions}}</h5>',
          });
        done();
      });
    });


    it('dgridNewPosition, check grid properties', (done) => {
      const grid = {
        backScroll: false,
        _total: 20000,
        refreshId: null,
        needBackScroll: false,

        formClass: 'ru.curs.demo.P1MainLyra',
        instanceId: 'grid1',
        context: {
          part1: 'part1',
          part2: 'part2',
          refreshParams:
            {
              filter: {
                filter: 'filter conditions',
              },
              sort: ['name', 'code'],
            },
        },
        oldSort: ['name', 'code'],
        oldFilter: {
          filter: 'filter conditions',
        },

        oldStart: 500,
        dgridOldPosition: 20,
        sortingOrFilteringChanged: false,
        limit: 0,

        firstLoading: false,
      };

      function setLabels() {
      }

      const store = createStore(grid, null, setLabels);
      const kwArgs = [{
        start: 250,
        end: 300,
      }];
      store._fetch(kwArgs);

      req.respond(200,
        {
          'Content-Type': 'application/json',
          'Content-Range': 'items 0-49/20000',
        },
        JSON.stringify(mockDataNewPosition));

      expect(grid.resScroll.length)
        .to
        .equal(50);
      expect(grid.resScroll[0].code)
        .to
        .equal('74000004000079300');
      expect(grid.resScroll[0].internalId)
        .to
        .deep
        .equal(['74000004000079300']);
      expect(grid.resScroll[0].internalAddData)
        .to
        .deep
        .equal({
          header: '<h5>Eto heder lira-grida</h5>',
          footer: '<h5>refreshParams: {selectKey=[74000004000079300], sort=[name, code], filter={filter1=filter conditions 1, filter2=filter conditions 2}}</h5>',
        });

      expect(grid.dgridNewPosition)
        .to
        .equal(12876);
      expect(grid.dgridNewPositionId)
        .to
        .equal('74000004000079300');
      expect(grid.dgridOldPosition)
        .to
        .equal(12876);

      done();
    });


    it('empty data, check grid properties && QueryResults data', (done) => {
      const grid = {
        backScroll: false,
        _total: 20000,
        refreshId: null,
        needBackScroll: false,

        formClass: 'ru.curs.demo.P1MainLyra',
        instanceId: 'grid1',
        context: {
          part1: 'part1',
          part2: 'part2',
          refreshParams:
            {
              filter: {
                filter: 'filter conditions',
              },
              sort: ['name', 'code'],
            },
        },
        oldSort: ['name', 'code'],
        oldFilter: {
          filter: 'filter conditions',
        },

        oldStart: 500,
        dgridOldPosition: 20,
        sortingOrFilteringChanged: false,
        limit: 0,

        firstLoading: false,
      };

      function setLabels() {
      }

      const store = createStore(grid, null, setLabels);
      const kwArgs = [{
        start: 250,
        end: 300,
      }];
      const results = store._fetch(kwArgs);

      req.respond(200,
        {
          'Content-Type': 'application/json',
          'Content-Range': 'items 0-49/20000',
        },
        JSON.stringify(mockDataEmpty));

      expect(grid.resScroll).to.be.undefined;

      results.then((data) => {
        expect(data.internalAddData)
          .to
          .deep
          .equal({
            header: '<h5>\'Showcase\' tipy stolbcov</h5>',
            footer: '<h5>refreshParams: {sort=[code], filter={filter=filter conditions}}</h5>',
          });
        done();
      });
    });


    it('setLabels, standard', () => {
      const grid = {
        backScroll: false,
        _total: 20000,
        refreshId: null,
        needBackScroll: false,

        formClass: 'ru.curs.demo.P1MainLyra',
        instanceId: 'grid1',
        context: {
          part1: 'part1',
          part2: 'part2',
          refreshParams:
            {
              filter: {
                filter: 'filter conditions',
              },
              sort: ['name', 'code'],
            },
        },
        oldSort: ['name', 'code'],
        oldFilter: {
          filter: 'filter conditions',
        },

        oldStart: 500,
        dgridOldPosition: 20,
        sortingOrFilteringChanged: false,
        limit: 0,

        firstLoading: false,
      };

      const setLabels = sinon.spy();

      const store = createStore(grid, null, setLabels);
      const kwArgs = [{
        start: 250,
        end: 300,
      }];
      store._fetch(kwArgs);

      req.respond(200,
        {
          'Content-Type': 'application/json',
          'Content-Range': 'items 0-49/20000',
        },
        JSON.stringify(mockData));

      setLabels.withArgs(
        '<h5>Eto heder lira-grida</h5>',
        '<h5>refreshParams: {sort=[name, code], filter={filter=filter conditions}}</h5>',
      ).should.have.been.calledOnce;
    });


    it('setLabels, empty data', () => {
      const grid = {
        backScroll: false,
        _total: 20000,
        refreshId: null,
        needBackScroll: false,

        formClass: 'ru.curs.demo.P1MainLyra',
        instanceId: 'grid1',
        context: {
          part1: 'part1',
          part2: 'part2',
          refreshParams:
            {
              filter: {
                filter: 'filter conditions',
              },
              sort: ['name', 'code'],
            },
        },
        oldSort: ['name', 'code'],
        oldFilter: {
          filter: 'filter conditions',
        },

        oldStart: 500,
        dgridOldPosition: 20,
        sortingOrFilteringChanged: false,
        limit: 0,

        firstLoading: false,
      };

      const setLabels = sinon.spy();

      const store = createStore(grid, null, setLabels);
      const kwArgs = [{
        start: 250,
        end: 300,
      }];
      store._fetch(kwArgs);

      req.respond(200,
        {
          'Content-Type': 'application/json',
          'Content-Range': 'items 0-49/20000',
        },
        JSON.stringify(mockDataEmpty));

      setLabels.withArgs(
        '<h5>\'Showcase\' tipy stolbcov</h5>',
        '<h5>refreshParams: {sort=[code], filter={filter=filter conditions}}</h5>',
      ).should.have.been.calledOnce;
    });


    it('setLabels, no addData', () => {
      const grid = {
        backScroll: false,
        _total: 20000,
        refreshId: null,
        needBackScroll: false,

        formClass: 'ru.curs.demo.P1MainLyra',
        instanceId: 'grid1',
        context: {
          part1: 'part1',
          part2: 'part2',
          refreshParams:
            {
              filter: {
                filter: 'filter conditions',
              },
              sort: ['name', 'code'],
            },
        },
        oldSort: ['name', 'code'],
        oldFilter: {
          filter: 'filter conditions',
        },

        oldStart: 500,
        dgridOldPosition: 20,
        sortingOrFilteringChanged: false,
        limit: 0,

        firstLoading: false,
      };

      const setLabels = sinon.spy();

      const store = createStore(grid, null, setLabels);
      const kwArgs = [{
        start: 250,
        end: 300,
      }];
      store._fetch(kwArgs);

      req.respond(200,
        {
          'Content-Type': 'application/json',
          'Content-Range': 'items 0-49/20000',
        },
        JSON.stringify(mockDataNoAddData));

      setLabels.should.have.not.been.called;
    });


    it('showMessage, error on the request', () => {
      const grid = {
        backScroll: false,
        _total: 20000,
        refreshId: null,
        needBackScroll: false,

        formClass: 'ru.curs.demo.P1MainLyra',
        instanceId: 'grid1',
        context: {
          part1: 'part1',
          part2: 'part2',
          refreshParams:
            {
              filter: {
                filter: 'filter conditions',
              },
              sort: ['name', 'code'],
            },
        },
        oldSort: ['name', 'code'],
        oldFilter: {
          filter: 'filter conditions',
        },

        oldStart: 500,
        dgridOldPosition: 20,
        sortingOrFilteringChanged: false,
        limit: 0,

        firstLoading: false,
      };

      const showMessage = sinon.spy();

      const store = createStore(grid, showMessage, null);
      const kwArgs = [{
        start: 250,
        end: 300,
      }];
      store._fetch(kwArgs);

      req.respond(500, {}, 'Server error');

      showMessage.withArgs('Server error').should.have.been.calledOnce;
    });
  });
});
