import {
  mockData,
  mockDataNewPosition,
  mockDataRowCss,
  mockMetadata,
  mockMetadataNoPrimaryKey,
  mockMetadataSummaryRow,
} from './const';
import createGrid from '../../src/grid';


describe('grid.js', () => {
  describe('create grid', () => {
    let grid = null;
    let req = null;
    before(() => {
      sinon.useFakeXMLHttpRequest().onCreate = function onCreate(xhr) {
        req = xhr;
      };

      function showMessage() {
      }

      function emitEvent() {
      }

      function setLabels() {
      }

      grid = createGrid(
        mockMetadata,
        'ru-curs-demo-P1MainLyra-grid1',
        'ru.curs.demo.P1MainLyra',
        'grid1',
        {
          part1: 'part1',
          part2: 'part2',
          refreshParams:
            {
              sort: ['name', 'code'],
              filter: {
                filter: 'filter conditions',
              },
            },
        },
        showMessage,
        emitEvent,
        setLabels,
      );
      grid.startup();

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

    it('check grid properties', () => {
      expect(grid.minRowsPerPage, 'minRowsPerPage')
        .to
        .equal(50);
      expect(grid.maxRowsPerPage, 'maxRowsPerPage')
        .to
        .equal(50);

      expect(grid.bufferRows, 'bufferRows')
        .to
        .equal(0);
      expect(grid.farOffRemoval, 'farOffRemoval')
        .to
        .equal(0);
      expect(grid.pagingDelay, 'pagingDelay')
        .to
        .equal(50);

      expect(grid.selectionMode, 'selectionMode')
        .to
        .equal('extended');
      expect(grid.allowTextSelection, 'allowTextSelection').to.be.true;

      expect(grid.showHeader, 'showHeader').to.be.true;

      expect(grid.loadingMessage, 'loadingMessage')
        .to
        .be
        .a('string');
      expect(grid.noDataMessage, 'noDataMessage')
        .to
        .be
        .a('string');

      expect(grid.deselectOnRefresh, 'deselectOnRefresh').to.be.false;
      expect(grid.keepScrollPosition, 'keepScrollPosition').to.be.false;

      expect(grid.backScroll, 'backScroll').to.be.false;
      expect(grid.resScroll, 'resScroll')
        .to
        .deep
        .equal(mockData);

      expect(grid.needBackScroll, 'needBackScroll').to.be.true;

      expect(grid.dgridOldPosition, 'dgridOldPosition')
        .to
        .equal(0);

      expect(grid.limit, 'limit')
        .to
        .equal(50);

      expect(grid.oldSort, 'oldSort')
        .to
        .deep
        .equal(['name', 'code']);
      expect(grid.oldFilter, 'oldFilter')
        .to
        .deep
        .equal({ filter: 'filter conditions' });

      expect(grid.formClass, 'formClass')
        .to
        .equal('ru.curs.demo.P1MainLyra');
      expect(grid.instanceId, 'instanceId')
        .to
        .equal('grid1');
      expect(grid.context, 'context')
        .to
        .deep
        .equal({
          part1: 'part1',
          part2: 'part2',
          refreshParams:
            {
              sort: ['name', 'code'],
              filter: {
                filter: 'filter conditions',
              },
            },
        });

      expect(grid.showFooter, 'showFooter').to.be.false;
      expect(grid.summary, 'summary').to.be.null;
    });

    it('check grid columns', () => {
      expect(grid.columns, 'columns')
        .to
        .be
        .an('object');

      expect(grid.columns.code.id, 'columns.code.id')
        .to
        .equal('code');
      expect(grid.columns.code.field, 'columns.code.field')
        .to
        .equal('code');

      expect(grid.columns.name.id, 'columns.name.id')
        .to
        .equal('name');
      expect(grid.columns.name.field, 'columns.name.field')
        .to
        .equal('name');
    });

    it('check grid rows', () => {
      expect(grid.row('63012000044007300').id)
        .to
        .deep
        .equal(['63012000044007300']);
      expect(grid.row('63012000044007300').data)
        .to
        .deep
        .equal({
          rnum: '10',
          internalId: ['63012000044007300'],
          field1: 'pole1_(Mikrorajon B) 1-aya Ozernaya',
          code: '63012000044007300',
          gninmb: '6350',
          recversion: '2',
          uno: '',
          ocatd: '36218848003',
          name: '(Mikrorajon B) 1-aya Ozernaya',
          field2: 'pole2_63012000044007300',
          socr: 'ul',
        });
    });

    it('check column reorder', (done) => {
      grid.columns.ocatd.width = 170;

      expect(grid.columns.ocatd.width, 'before reorder')
        .to
        .equal(170);

      const ev = new Event('dgrid-columnreorder');
      ev.grid = grid;
      grid.domNode.dispatchEvent(ev);

      setTimeout(() => {
        expect(grid.columns.ocatd.width, 'after reorder')
          .to
          .equal(170);
        done();
      }, 100);
    });

    it('check add column', () => {
      grid.columns.newField = {
        field: 'newField',
        name: 'newName',
      };

      grid.set('columns', grid.columns);

      expect(grid.columns.newField.id)
        .to
        .equal('newField');
    });
  });


  describe('row css', () => {
    let grid = null;
    let req = null;
    before(() => {
      sinon.useFakeXMLHttpRequest().onCreate = function onCreate(xhr) {
        req = xhr;
      };

      const el = document.createElement('div');
      el.id = 'ru-curs-demo-P1MainLyra-grid1';
      document.body.appendChild(el);

      function showMessage() {
      }

      function emitEvent() {
      }

      function setLabels() {
      }

      grid = createGrid(
        mockMetadata,
        'ru-curs-demo-P1MainLyra-grid1',
        'ru.curs.demo.P1MainLyra',
        'grid1',
        {
          part1: 'part1',
          part2: 'part2',
          refreshParams:
            {
              sort: ['name', 'code'],
              filter: {
                filter: 'filter conditions',
              },
            },
        },
        showMessage,
        emitEvent,
        setLabels,
      );
      grid.startup();

      req.respond(200,
        {
          'Content-Type': 'application/json',
          'Content-Range': 'items 0-49/20000',
        },
        JSON.stringify(mockDataRowCss));
    });
    after(() => {
      sinon.useFakeXMLHttpRequest()
        .restore();

      document.body.querySelector('#ru-curs-demo-P1MainLyra-grid1')
        .remove();
    });

    it('check row css', () => {
      expect(grid.row('63012000044007300').id)
        .to
        .deep
        .equal(['63012000044007300']);
      expect(grid.row('63012000044007300').data)
        .to
        .deep
        .equal({
          rnum: '10',
          internalId: ['63012000044007300'],
          code: '63012000044007300',
          gninmb: '6350',
          recversion: '2',
          uno: '',
          ocatd: '36218848003',
          name: '(Mikrorajon B) 1-aya Ozernaya',
          socr: 'ul',
          recordProperties: { rowstyle: 'jslivegrid-record-bold jslivegrid-record-italic' },
        });
      expect(grid.row('63012000044007300').element.className)
        .to
        .include('jslivegrid-record-bold jslivegrid-record-italic');
    });
  });

  describe('summary row', () => {
    let grid = null;
    let req = null;
    beforeEach(() => {
      sinon.useFakeXMLHttpRequest().onCreate = function onCreate(xhr) {
        req = xhr;
      };

      const el = document.createElement('div');
      el.id = 'ru-curs-demo-P1MainLyra-grid1';
      el.style = 'width:50px;';
      document.body.appendChild(el);

      function showMessage() {
      }

      function emitEvent() {
      }

      function setLabels() {
      }

      grid = createGrid(
        mockMetadataSummaryRow,
        'ru-curs-demo-P1MainLyra-grid1',
        'ru.curs.demo.P1MainLyra',
        'grid1',
        {
          part1: 'part1',
          part2: 'part2',
          refreshParams:
            {
              sort: ['name', 'code'],
              filter: {
                filter: 'filter conditions',
              },
            },
        },
        showMessage,
        emitEvent,
        setLabels,
      );
      grid.startup();

      req.respond(200,
        {
          'Content-Type': 'application/json',
          'Content-Range': 'items 0-49/20000',
        },
        JSON.stringify(mockData));
    });
    afterEach(() => {
      sinon.useFakeXMLHttpRequest()
        .restore();

      grid = null;
      req = null;

      document.body.querySelector('#ru-curs-demo-P1MainLyra-grid1')
        .remove();
    });

    it('check grid properties', () => {
      expect(grid.showFooter, 'showFooter').to.be.true;
      expect(grid.summary)
        .to
        .deep
        .equal({
          rnum: 'RNUM',
          code: 'CODE',
          gninmb: 'GNINMB',
          ocatd: 'OCATD',
          uno: 'UNO',
          name: 'One two three four five six seven eight nine ten One two three four five six seven eight nine ten',
          socr: 'SOCR',
        });
    });

    it('check scroll', () => {
      grid.summaryAreaNode.scrollLeft = 100;

      expect(grid.summaryAreaNode.scrollLeft, 'before scroll')
        .to
        .equal(100);

      grid.domNode.dispatchEvent(new Event('scroll'));

      expect(grid.summaryAreaNode.scrollLeft, 'after scroll')
        .to
        .equal(0);
    });

    it('check column reorder', (done) => {
      grid.columns.ocatd.width = 170;

      expect(grid.columns.ocatd.width, 'before reorder')
        .to
        .equal(170);

      const ev = new Event('dgrid-columnreorder');
      ev.grid = grid;
      grid.domNode.dispatchEvent(ev);

      setTimeout(() => {
        expect(grid.columns.ocatd.width, 'after reorder').to.be.undefined;
        done();
      }, 100);
    });

    it('check add column', () => {
      grid.summary.newField = 'NEWFIELD';

      grid.columns.newField = {
        field: 'newField',
        name: 'newName',
      };

      grid.set('columns', grid.columns);

      expect(grid.footerNode.innerText)
        .to
        .include('NEWFIELD');
    });

    it('check setSummary', () => {
      grid.setSummary('test Summary');
      expect(grid.summary)
        .to
        .equal('test Summary');

      grid._started = false;

      grid.setSummary('test Summary 2');
      expect(grid.summary)
        .to
        .equal('test Summary 2');
    });

    it('check adjustFooterCellsWidths', () => {
      grid._resizedColumns = true;
      grid.adjustFooterCellsWidths();
      expect(grid._resizedColumns).to.be.true;
    });
  });


  describe('emitEvent', () => {
    let grid = null;
    let req = null;
    const emitEvent = sinon.spy();
    beforeEach(() => {
      sinon.useFakeXMLHttpRequest().onCreate = function onCreate(xhr) {
        req = xhr;
      };

      const el = document.createElement('div');
      el.id = 'ru-curs-demo-P1MainLyra-grid1';
      document.body.appendChild(el);

      function showMessage() {
      }

      function setLabels() {
      }

      grid = createGrid(
        mockMetadata,
        'ru-curs-demo-P1MainLyra-grid1',
        'ru.curs.demo.P1MainLyra',
        'grid1',
        {
          part1: 'part1',
          part2: 'part2',
          refreshParams:
            {
              sort: ['name', 'code'],
              filter: {
                filter: 'filter conditions',
              },
            },
        },
        showMessage,
        emitEvent,
        setLabels,
      );
      grid.startup();

      req.respond(200,
        {
          'Content-Type': 'application/json',
          'Content-Range': 'items 0-49/20000',
        },
        JSON.stringify(mockData));
    });
    afterEach(() => {
      sinon.useFakeXMLHttpRequest()
        .restore();

      emitEvent.resetHistory();

      document.body.querySelector('#ru-curs-demo-P1MainLyra-grid1')
        .remove();
    });

    it('columns-info', () => {
      emitEvent.should.have.been.calledOnceWith('columns-info', [{
        id: 'name',
        field: 'name',
        hidden: false,
        sortable: true,
        label: 'Nazvanie',
        sortingAvailable: true,
        className: 'lyra-type-varchar',
      }, {
        id: 'rnum',
        field: 'rnum',
        hidden: false,
        sortable: true,
        label: 'rnum',
        sortingAvailable: true,
        className: 'lyra-type-int',
      }, {
        id: 'code',
        field: 'code',
        hidden: false,
        sortable: true,
        label: 'code',
        sortingAvailable: false,
        className: 'lyra-type-varchar',
      }, {
        id: 'socr',
        field: 'socr',
        hidden: false,
        sortable: true,
        label: 'socr',
        sortingAvailable: false,
        className: 'lyra-type-varchar',
      }, {
        id: 'gninmb',
        field: 'gninmb',
        hidden: false,
        sortable: true,
        label: 'gninmb',
        sortingAvailable: false,
        className: 'lyra-type-varchar',
      }, {
        id: 'uno',
        field: 'uno',
        hidden: true,
        sortable: true,
        label: 'uno',
        sortingAvailable: false,
        className: 'lyra-type-varchar',
      }, {
        id: 'ocatd',
        field: 'ocatd',
        hidden: false,
        sortable: true,
        label: 'ocatd',
        sortingAvailable: true,
        className: 'lyra-type-varchar',
      }, {
        id: 'field1',
        field: 'field1',
        hidden: false,
        sortable: false,
        label: 'Unbound pole1',
        sortingAvailable: false,
        className: 'lyra-type-varchar',
      }, {
        id: 'field2',
        field: 'field2',
        hidden: false,
        sortable: false,
        label: 'Unbound pole2',
        sortingAvailable: false,
        className: 'lyra-type-varchar',
      }]);
    });

    it('select', () => {
      grid.select('63012000044007300');
      emitEvent.withArgs('select', {
        currentColId: 'name',
        currentRowId: ['63028000006005400'],
        currentRowData: {
          internalAddData: {
            header: '<h5>Eto heder lira-grida</h5>',
            footer: '<h5>refreshParams: {sort=[name, code], filter={filter=filter conditions}}</h5>',
          },
          rnum: '1',
          internalId: ['63028000006005400'],
          field1: 'pole1_(kottedzhnyj poselok Mastryuki)',
          code: '63028000006005400',
          gninmb: '6330',
          recversion: '2',
          uno: '',
          ocatd: '36214820002',
          name: '(kottedzhnyj poselok Mastryuki)',
          field2: 'pole2_63028000006005400',
          socr: 'p',
        },
        selection: ['63012000044007300'],
      }).should.have.been.calledOnce;
    });

    it('select, no col or row', () => {
      const ev = new Event('dgrid-select');
      ev.grid = { _focusedNode: '' };
      grid.domNode.dispatchEvent(ev);
      emitEvent.withArgs('select').should.have.not.been.called;
    });

    it('click', () => {
      grid.cell('63012000044007300', 'code')
        .element
        .click();
      emitEvent.withArgs('click', {
        currentColId: 'code',
        currentRowId: ['63012000044007300'],
        currentRowData: {
          rnum: '10',
          internalId: ['63012000044007300'],
          field1: 'pole1_(Mikrorajon B) 1-aya Ozernaya',
          code: '63012000044007300',
          gninmb: '6350',
          recversion: '2',
          uno: '',
          ocatd: '36218848003',
          name: '(Mikrorajon B) 1-aya Ozernaya',
          field2: 'pole2_63012000044007300',
          socr: 'ul',
        },
        selection: [],
      }).should.have.been.calledOnce;
    });

    it('click, no col or row', () => {
      grid.row('63012000044007300')
        .element
        .click();
      emitEvent.withArgs('click').should.have.not.been.called;
    });

    it('dblclick', () => {
      grid.select('63012000044007300');
      grid.cell('63012000044007300', 'code')
        .element
        .dispatchEvent(new Event('dblclick', { bubbles: true }));
      emitEvent.withArgs('dblclick', {
        currentColId: 'code',
        currentRowId: ['63012000044007300'],
        currentRowData: {
          rnum: '10',
          internalId: ['63012000044007300'],
          field1: 'pole1_(Mikrorajon B) 1-aya Ozernaya',
          code: '63012000044007300',
          gninmb: '6350',
          recversion: '2',
          uno: '',
          ocatd: '36218848003',
          name: '(Mikrorajon B) 1-aya Ozernaya',
          field2: 'pole2_63012000044007300',
          socr: 'ul',
        },
        selection: ['63012000044007300'],
      }).should.have.been.calledOnce;
    });

    it('dblclick, no col or row', () => {
      grid.select('63012000044007300');
      grid.row('63012000044007300')
        .element
        .dispatchEvent(new Event('dblclick', { bubbles: true }));
      emitEvent.withArgs('dblclick').should.have.not.been.called;
    });
  });


  describe('internal sorting', () => {
    function showMessage() {
    }

    function emitEvent() {
    }

    function setLabels() {
    }

    let req = null;
    beforeEach(() => {
      sinon.useFakeXMLHttpRequest().onCreate = function onCreate(xhr) {
        req = xhr;
      };
    });
    afterEach(() => {
      sinon.useFakeXMLHttpRequest()
        .restore();
    });

    it('ascending', () => {
      const grid = createGrid(
        mockMetadata,
        'ru-curs-demo-P1MainLyra-grid1',
        'ru.curs.demo.P1MainLyra',
        'grid1',
        {
          part1: 'part1',
          part2: 'part2',
          refreshParams:
            {
              sort: ['name', 'code'],
              filter: {
                filter: 'filter conditions',
              },
            },
        },
        showMessage,
        emitEvent,
        setLabels,
      );
      grid.startup();

      req.respond(200,
        {
          'Content-Type': 'application/json',
          'Content-Range': 'items 0-49/20000',
        },
        JSON.stringify(mockData));


      grid.columns.rnum
        .headerNode
        .click();

      expect(grid.context.refreshParams.sort)
        .to
        .deep
        .equal(['rnum', 'code']);
    });

    it('descending', () => {
      const grid = createGrid(
        mockMetadata,
        'ru-curs-demo-P1MainLyra-grid1',
        'ru.curs.demo.P1MainLyra',
        'grid1',
        {
          part1: 'part1',
          part2: 'part2',
          refreshParams:
            {
              sort: ['name', 'code'],
              filter: {
                filter: 'filter conditions',
              },
            },
        },
        showMessage,
        emitEvent,
        setLabels,
      );
      grid.startup();

      req.respond(200,
        {
          'Content-Type': 'application/json',
          'Content-Range': 'items 0-49/20000',
        },
        JSON.stringify(mockData));

      grid.columns.rnum
        .headerNode
        .click();
      grid.columns.rnum
        .headerNode
        .click();

      expect(grid.context.refreshParams.sort)
        .to
        .deep
        .equal(['rnum desc', 'code desc']);
    });


    it('no primary key', () => {
      const grid = createGrid(
        mockMetadataNoPrimaryKey,
        'ru-curs-demo-P1MainLyra-grid1',
        'ru.curs.demo.P1MainLyra',
        'grid1',
        {
          part1: 'part1',
          part2: 'part2',
          refreshParams:
            {
              sort: ['name', 'code'],
              filter: {
                filter: 'filter conditions',
              },
            },
        },
        showMessage,
        emitEvent,
        setLabels,
      );
      grid.startup();

      req.respond(200,
        {
          'Content-Type': 'application/json',
          'Content-Range': 'items 0-49/20000',
        },
        JSON.stringify(mockData));


      grid.columns.rnum
        .headerNode
        .click();

      expect(grid.context.refreshParams.sort)
        .to
        .deep
        .equal(['rnum']);
    });

    it('sort is part of the primary key', () => {
      const grid = createGrid(
        mockMetadata,
        'ru-curs-demo-P1MainLyra-grid1',
        'ru.curs.demo.P1MainLyra',
        'grid1',
        {
          part1: 'part1',
          part2: 'part2',
          refreshParams:
            {
              sort: ['name', 'code'],
              filter: {
                filter: 'filter conditions',
              },
            },
        },
        showMessage,
        emitEvent,
        setLabels,
      );
      grid.startup();

      req.respond(200,
        {
          'Content-Type': 'application/json',
          'Content-Range': 'items 0-49/20000',
        },
        JSON.stringify(mockData));


      grid.columns.code
        .headerNode
        .click();

      expect(grid.context.refreshParams.sort)
        .to
        .deep
        .equal(['code']);
    });
  });


  describe('positioning by primary key ', () => {
    let grid = null;
    let req = null;
    const emitEvent = sinon.spy();
    beforeEach(() => {
      sinon.useFakeXMLHttpRequest().onCreate = function onCreate(xhr) {
        req = xhr;
      };

      const el = document.createElement('div');
      el.id = 'ru-curs-demo-P1MainLyra-grid1';
      document.body.appendChild(el);

      function showMessage() {
      }

      function setLabels() {
      }

      grid = createGrid(
        mockMetadata,
        'ru-curs-demo-P1MainLyra-grid1',
        'ru.curs.demo.P1MainLyra',
        'grid1',
        {
          part1: 'part1',
          part2: 'part2',
          refreshParams:
            {
              sort: ['name', 'code'],
              filter: {
                filter: 'filter conditions',
              },
            },
        },
        showMessage,
        emitEvent,
        setLabels,
      );
      grid.startup();

      req.respond(200,
        {
          'Content-Type': 'application/json',
          'Content-Range': 'items 0-49/20000',
        },
        JSON.stringify(mockDataNewPosition));
    });
    afterEach(() => {
      sinon.useFakeXMLHttpRequest()
        .restore();

      emitEvent.resetHistory();

      document.body.querySelector('#ru-curs-demo-P1MainLyra-grid1')
        .remove();
    });

    it('first loading', () => {
      expect(grid.dgridNewPosition)
        .to
        .equal(12876);
      expect(grid.dgridNewPositionId)
        .to
        .equal('74000004000079300');

      grid.firstLoading = true;
      grid.domNode.dispatchEvent(new Event('dgrid-refresh-complete'));

      expect(grid.dgridNewPosition).to.be.null;
      expect(grid.dgridNewPositionId).to.be.null;
      expect(grid.firstLoading).to.be.false;
    });

    it('not first loading', () => {
      expect(grid.dgridNewPosition)
        .to
        .equal(12876);
      expect(grid.dgridNewPositionId)
        .to
        .equal('74000004000079300');

      grid.firstLoading = false;
      grid.domNode.dispatchEvent(new Event('dgrid-refresh-complete'));

      expect(grid.dgridNewPosition)
        .to
        .equal(12876);
      expect(grid.dgridNewPositionId)
        .to
        .equal('74000004000079300');
    });
  });
});
