import { shallowMount } from '@vue/test-utils';
import SockJS from 'sockjs-client';
import { mockData, mockMetadata } from './const';
import LyraGrid, { lyraGridSocket } from '../../src/LyraGrid.vue';

lyraGridSocket.reconnectDelay = 0;


describe('LyraGrid.vue', () => {
  describe('create lyra grid', () => {
    let req;
    let wrapper;
    before((done) => {
      const mockAxios = {
        post: sinon.stub()
          .returns(Promise.resolve({ data: mockMetadata })),
      };
      LyraGrid.__Rewire__('axios', mockAxios);

      sinon.useFakeXMLHttpRequest().onCreate = function onCreate(xhr) {
        req = xhr;
      };

      wrapper = shallowMount(LyraGrid, {
        propsData: {
          formclass: 'ru.curs.demo.P1MainLyra',
          instanceid: 'grid1',
          context: {
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
        },
      });

      setImmediate(() => {
        wrapper.vm.grid.startup();
        req.respond(200,
          {
            'Content-Type': 'application/json',
            'Content-Range': 'items 0-49/20000',
          },
          JSON.stringify(mockData));
        done();
      });
    });
    after(() => {
      sinon.useFakeXMLHttpRequest()
        .restore();
    });

    it('check lyra grid properties', () => {
      expect(wrapper.vm.grid.minRowsPerPage, 'minRowsPerPage')
        .to
        .equal(50);
      expect(wrapper.vm.grid.maxRowsPerPage, 'maxRowsPerPage')
        .to
        .equal(50);

      expect(wrapper.vm.grid.bufferRows, 'bufferRows')
        .to
        .equal(0);
      expect(wrapper.vm.grid.farOffRemoval, 'farOffRemoval')
        .to
        .equal(0);
      expect(wrapper.vm.grid.pagingDelay, 'pagingDelay')
        .to
        .equal(50);

      expect(wrapper.vm.grid.selectionMode, 'selectionMode')
        .to
        .equal('extended');
      expect(wrapper.vm.grid.allowTextSelection, 'allowTextSelection').to.be.true;

      expect(wrapper.vm.grid.showHeader, 'showHeader').to.be.true;

      expect(wrapper.vm.grid.loadingMessage, 'loadingMessage')
        .to
        .be
        .a('string');
      expect(wrapper.vm.grid.noDataMessage, 'noDataMessage')
        .to
        .be
        .a('string');

      expect(wrapper.vm.grid.deselectOnRefresh, 'deselectOnRefresh').to.be.false;
      expect(wrapper.vm.grid.keepScrollPosition, 'keepScrollPosition').to.be.false;

      expect(wrapper.vm.grid.backScroll, 'backScroll').to.be.false;
      expect(wrapper.vm.grid.resScroll, 'resScroll')
        .to
        .deep
        .equal(mockData);

      expect(wrapper.vm.grid.needBackScroll, 'needBackScroll').to.be.true;

      expect(wrapper.vm.grid.dgridOldPosition, 'dgridOldPosition')
        .to
        .equal(0);

      expect(wrapper.vm.grid.limit, 'limit')
        .to
        .equal(50);

      expect(wrapper.vm.grid.oldSort, 'oldSort')
        .to
        .deep
        .equal(['name', 'code']);
      expect(wrapper.vm.grid.oldFilter, 'oldFilter')
        .to
        .deep
        .equal({ filter: 'filter conditions' });

      expect(wrapper.vm.grid.formClass, 'formClass')
        .to
        .equal('ru.curs.demo.P1MainLyra');
      expect(wrapper.vm.grid.instanceId, 'instanceId')
        .to
        .equal('grid1');
      expect(wrapper.vm.grid.context, 'context')
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

      expect(wrapper.vm.grid.showFooter, 'showFooter').to.be.false;
      expect(wrapper.vm.grid.summary, 'summary').to.be.null;
    });

    it('check lyra grid columns', () => {
      expect(wrapper.vm.grid.columns, 'columns')
        .to
        .be
        .an('object');

      expect(wrapper.vm.grid.columns.code.id, 'columns.code.id')
        .to
        .equal('code');
      expect(wrapper.vm.grid.columns.code.field, 'columns.code.field')
        .to
        .equal('code');

      expect(wrapper.vm.grid.columns.name.id, 'columns.name.id')
        .to
        .equal('name');
      expect(wrapper.vm.grid.columns.name.field, 'columns.name.field')
        .to
        .equal('name');
    });

    it('check lyra grid rows', () => {
      expect(wrapper.vm.grid.row('63012000044007300').id)
        .to
        .deep
        .equal(['63012000044007300']);
      expect(wrapper.vm.grid.row('63012000044007300').data)
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

    it('check lyra grid rendering', () => {
      expect(wrapper.text())
        .to
        .include('refreshParams: {sort=[name, code], filter={filter=filter conditions}}');
    });
  });

  describe('events emitted to the lyra grid', () => {
    let req;
    let wrapper;
    before((done) => {
      const mockAxios = {
        post: sinon.stub()
          .returns(Promise.resolve({ data: mockMetadata })),
      };
      LyraGrid.__Rewire__('axios', mockAxios);

      sinon.useFakeXMLHttpRequest().onCreate = function onCreate(xhr) {
        req = xhr;
      };

      wrapper = shallowMount(LyraGrid, {
        propsData: {
          formclass: 'ru.curs.demo.P1MainLyra',
          instanceid: 'grid1',
          context: {
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
        },
      });

      setImmediate(() => {
        wrapper.vm.grid.startup();
        req.respond(200,
          {
            'Content-Type': 'application/json',
            'Content-Range': 'items 0-49/20000',
          },
          JSON.stringify(mockData));
        done();
      });
    });
    after(() => {
      sinon.useFakeXMLHttpRequest()
        .restore();
    });

    it('refresh', () => {
      wrapper.vm.$emit('refresh', {
        refreshParams:
          {
            sort: ['rnum desc'],
            filter: {
              filter: 'filter conditions 2',
            },
          },
      });

      expect(wrapper.vm.grid.context)
        .to
        .deep
        .equal({
          refreshParams:
            {
              sort: ['rnum desc'],
              filter: {
                filter: 'filter conditions 2',
              },
            },
        });
    });

    it('set-columns-visibility', () => {
      expect(wrapper.vm.grid.columns.code.hidden, 'columns.code.hidden before').to.be.false;
      expect(wrapper.vm.grid.columns.socr.hidden, 'columns.socr.hidden before').to.be.false;

      wrapper.vm.$emit('set-columns-visibility', [{
        id: 'code',
        visible: false,
      }, {
        id: 'socr',
        visible: false,
      }]);

      expect(wrapper.vm.grid.columns.code.hidden, 'columns.code.hidden after').to.be.true;
      expect(wrapper.vm.grid.columns.socr.hidden, 'columns.socr.hidden after').to.be.true;
    });
  });

  describe('events emitted by the lyra grid', () => {
    let req;
    let wrapper;
    beforeEach((done) => {
      const mockAxios = {
        post: sinon.stub()
          .returns(Promise.resolve({ data: mockMetadata })),
      };
      LyraGrid.__Rewire__('axios', mockAxios);

      sinon.useFakeXMLHttpRequest().onCreate = function onCreate(xhr) {
        req = xhr;
      };

      const el = document.createElement('div');
      el.id = 'ru-curs-demo-P1MainLyra-grid1';
      document.body.appendChild(el);

      wrapper = shallowMount(LyraGrid, {
        propsData: {
          formclass: 'ru.curs.demo.P1MainLyra',
          instanceid: 'grid1',
          context: {
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
        },
      });

      setImmediate(() => {
        wrapper.vm.grid.startup();
        req.respond(200,
          {
            'Content-Type': 'application/json',
            'Content-Range': 'items 0-49/20000',
          },
          JSON.stringify(mockData));
        done();
      });
    });
    afterEach(() => {
      sinon.useFakeXMLHttpRequest()
        .restore();

      document.body.querySelector('#ru-curs-demo-P1MainLyra-grid1')
        .remove();
    });

    it('columns-info', () => {
      wrapper.emitted()['columns-info'].length
        .should.be.equal(1);

      wrapper.emitted()['columns-info'][0]
        .should.deep.equal([
          'ru.curs.demo.P1MainLyra',
          'grid1',
          [{
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
          }],
        ]);
    });

    it('select', () => {
      wrapper.vm.grid.select('63012000044007300');

      wrapper.emitted()
        .select
        .length
        .should
        .be
        .equal(1);

      wrapper.emitted().select[0]
        .should.deep.equal([
          'ru.curs.demo.P1MainLyra',
          'grid1',
          {
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
          },
        ]);
    });

    it('click', () => {
      wrapper.vm.grid.cell('63012000044007300', 'code')
        .element
        .click();

      wrapper.emitted()
        .click
        .length
        .should
        .be
        .equal(1);

      wrapper.emitted().click[0]
        .should.deep.equal([
          'ru.curs.demo.P1MainLyra',
          'grid1',
          {
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
          },
        ]);
    });

    it('dblclick', () => {
      wrapper.vm.grid.select('63012000044007300');
      wrapper.vm.grid.cell('63012000044007300', 'code')
        .element
        .dispatchEvent(new Event('dblclick', { bubbles: true }));

      wrapper.emitted()
        .dblclick
        .length
        .should
        .be
        .equal(1);

      wrapper.emitted().dblclick[0]
        .should.deep.equal([
          'ru.curs.demo.P1MainLyra',
          'grid1',
          {
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
          },
        ]);
    });
  });

  describe('lyra grid div not found', () => {
    let wrapper;
    before(() => {
      const mockAxios = {
        post: sinon.stub()
          .returns(Promise.resolve({ data: mockMetadata })),
      };
      LyraGrid.__Rewire__('axios', mockAxios);

      wrapper = shallowMount(LyraGrid, {
        propsData: {
          formclass: 'ru.curs.demo.P1MainLyra',
          instanceid: 'grid1',
          context: {
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
        },
      });

      wrapper.element.querySelector('#ru-curs-demo-P1MainLyra-grid1')
        .remove();
    });

    it('lyra grid div not found', (done) => {
      setImmediate(() => {
        expect(wrapper.vm.grid).to.be.undefined;
        done();
      });
    });
  });

  describe('showMessage(message)', () => {
    let req;
    let wrapper;
    before((done) => {
      const mockAxios = {
        post: sinon.stub()
          .returns(Promise.resolve({ data: mockMetadata })),
      };
      LyraGrid.__Rewire__('axios', mockAxios);

      sinon.useFakeXMLHttpRequest().onCreate = function onCreate(xhr) {
        req = xhr;

        window.getLyraConfig = function getLyraConfig() {
          return {
            showMessageFunction() {
            },
          };
        };
      };

      wrapper = shallowMount(LyraGrid, {
        propsData: {
          formclass: 'ru.curs.demo.P1MainLyra',
          instanceid: 'grid1',
          context: {
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
        },
      });

      setImmediate(() => {
        wrapper.vm.grid.startup();
        req.respond(200,
          {
            'Content-Type': 'application/json',
            'Content-Range': 'items 0-49/20000',
          },
          JSON.stringify(mockData));
        done();
      });
    });
    after(() => {
      sinon.useFakeXMLHttpRequest()
        .restore();

      window.getLyraConfig = null;
    });

    it('showMessage(message)', () => {
      expect(() => {
        wrapper.vm.showMessage('error');
      })
        .to
        .throw(Error, 'error');
    });
  });

  describe('get metadata request', () => {
    it('check request headers', () => {
      const post = sinon.stub()
        .returns(Promise.resolve({ data: mockMetadata }));
      const mockAxios = { post };
      LyraGrid.__Rewire__('axios', mockAxios);

      shallowMount(LyraGrid, {
        propsData: {
          formclass: 'ru.curs.demo.P1MainLyra',
          instanceid: 'grid1',
          context: {
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
        },
      });
      expect(post.args[0][2].headers['Content-Type'])
        .to
        .have
        .string('application/json');
      expect(post.args[0][2].headers['X-Requested-With']).to.be.undefined;
    });

    it('check request params', () => {
      const post = sinon.stub()
        .returns(Promise.resolve({ data: mockMetadata }));
      const mockAxios = { post };
      LyraGrid.__Rewire__('axios', mockAxios);

      shallowMount(LyraGrid, {
        propsData: {
          formclass: 'ru.curs.demo.P1MainLyra',
          instanceid: 'grid1',
          context: {
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
        },
      });
      expect(post.args[0][1])
        .to
        .deep
        .equal({
          formClass: 'ru.curs.demo.P1MainLyra',
          instanceId: 'grid1',
          clientParams: {
            part1: 'part1',
            part2: 'part2',
            refreshParams: {
              sort: ['name', 'code'],
              filter: { filter: 'filter conditions' },
            },
          },
        });
    });

    it('error on the request', (done) => {
      const requestError = new Error('Server error');
      const mockAxios = {
        post: sinon.stub()
          .returns(Promise.reject(requestError)),
      };
      LyraGrid.__Rewire__('axios', mockAxios);

      const showMessage = sinon.spy(LyraGrid.methods, 'showMessage');

      shallowMount(LyraGrid, {
        propsData: {
          formclass: 'ru.curs.demo.P1MainLyra',
          instanceid: 'grid1',
          context: {
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
        },
      });

      setImmediate(() => {
        showMessage.should.have.been.calledOnceWith(requestError);
        done();
      });
    });
  });

  describe('scroll back', () => {
    let req;
    let wrapper;
    beforeEach((done) => {
      const mockAxios = {
        post: sinon.stub()
          .returns(Promise.resolve({ data: mockMetadata })),
      };
      LyraGrid.__Rewire__('axios', mockAxios);

      sinon.useFakeXMLHttpRequest().onCreate = function onCreate(xhr) {
        req = xhr;
      };

      const el = document.createElement('div');
      el.id = 'ru-curs-demo-P1MainLyra-grid1';
      document.body.appendChild(el);

      wrapper = shallowMount(LyraGrid, {
        propsData: {
          formclass: 'ru.curs.demo.P1MainLyra',
          instanceid: 'grid1',
          context: {
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
        },
      });

      setImmediate(() => {
        wrapper.vm.grid.startup();
        req.respond(200,
          {
            'Content-Type': 'application/json',
            'Content-Range': 'items 0-49/20000',
          },
          JSON.stringify(mockData));
        done();
      });
    });
    afterEach(() => {
      sinon.useFakeXMLHttpRequest()
        .restore();

      document.body.querySelector('#ru-curs-demo-P1MainLyra-grid1')
        .parentElement.__vue__ = null;
      document.body.querySelector('#ru-curs-demo-P1MainLyra-grid1')
        .remove();
    });

    it('scroll back', () => {
      expect(wrapper.vm.grid.getScrollPosition().y, 'before')
        .to
        .equal(0);

      const div = document.body.querySelector('#ru-curs-demo-P1MainLyra-grid1');
      div.parentElement.__vue__ = { grid: wrapper.vm.grid };
      lyraGridSocket.scrollBackGrid({
        body: JSON.stringify({
          dgridId: 'ru-curs-demo-P1MainLyra-grid1',
          position: 100,
        }),
      });

      expect(wrapper.vm.grid.getScrollPosition().y, 'after')
        .to
        .within(1000, 3000);
    });

    it('vue not found in div', () => {
      expect(wrapper.vm.grid.getScrollPosition(), 'before')
        .to
        .deep
        .equal({
          x: 0,
          y: 0,
        });

      lyraGridSocket.scrollBackGrid({
        body: JSON.stringify({
          dgridId: 'ru-curs-demo-P1MainLyra-grid1',
          position: 100,
        }),
      });

      expect(wrapper.vm.grid.getScrollPosition(), 'after')
        .to
        .deep
        .equal({
          x: 0,
          y: 0,
        });
    });
  });

  describe('lyraGridSocket', () => {
    it('initialize', () => {
      lyraGridSocket.webSocket.readyState = 2;

      lyraGridSocket.onConnect();

      expect(lyraGridSocket._stompHandler._subscriptions['lyra-grid'].name)
        .to
        .equal('scrollBackGrid');
    });

    it('sockJS version', () => {
      expect(SockJS.version)
        .to
        .equal('1.4.0');
    });
  });
});
