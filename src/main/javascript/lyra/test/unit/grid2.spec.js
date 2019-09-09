import {
  getSelection,
  refreshGrid,
  scrollBack,
  setColumnsVisibility,
  setExternalSorting,
} from '../../src/grid2';

describe('grid2.js', () => {
  describe('scrollBack(grid, position)', () => {
    it('needBackScroll, pos >= 0', () => {
      const grid = {
        needBackScroll: true,
        rowHeight: 16,
        getScrollPosition() {
          return {
            x: 0,
            y: 100,
          };
        },
        scrollTo: sinon.spy(),
      };

      scrollBack(grid, 300);

      grid.scrollTo.should.have.been.calledOnceWith({
        x: 0,
        y: 4804,
      });
    });

    it('needBackScroll, pos < 0', () => {
      const grid = {
        needBackScroll: true,
        rowHeight: -16,
        getScrollPosition() {
          return {
            x: 0,
            y: 100,
          };
        },
        scrollTo: sinon.spy(),
      };

      scrollBack(grid, 300);

      grid.scrollTo.should.have.not.been.called;
    });

    it('not needBackScroll', () => {
      const grid = {
        needBackScroll: false,
        rowHeight: 16,
        getScrollPosition() {
          return {
            x: 0,
            y: 100,
          };
        },
        scrollTo: sinon.spy(),
      };

      scrollBack(grid, 300);

      grid.scrollTo.should.have.not.been.called;
    });
  });

  describe('getSelection(grid)', () => {
    it('getSelection(grid)', () => {
      const grid = {
        selection: {
          code1: true,
          code2: false,
          code3: true,
        },
      };
      getSelection(grid)
        .should
        .deep
        .equal(['code1', 'code3']);
    });
  });

  describe('setExternalSorting(columns, sort)', () => {
    it('ascending', () => {
      const columns = [
        { id: 'name' },
        { id: 'rnum' },
        { id: 'code' }];

      setExternalSorting(columns, ['name', 'code']);

      columns.should.deep.equal([
        {
          id: 'name',
          sortingPic: 'a1',
        }, {
          id: 'rnum',
          sortingPic: null,
        }, {
          id: 'code',
          sortingPic: 'a2',
        }]);
    });

    it('descending', () => {
      const columns = [
        { id: 'name' },
        { id: 'rnum' },
        { id: 'code' }];

      setExternalSorting(columns, ['name desc', 'code desc']);

      columns.should.deep.equal([
        {
          id: 'name',
          sortingPic: 'd1',
        }, {
          id: 'rnum',
          sortingPic: null,
        }, {
          id: 'code',
          sortingPic: 'd2',
        }]);
    });
  });

  describe('refreshGrid(grid, context, showMessage)', () => {
    it('selectKey = current, sort and filter did not change', () => {
      const context = {
        part1: 'part1',
        part2: 'part2',
        refreshParams:
          {
            selectKey: ['current'],
            sort: ['name', 'code'],
            filter: {
              filter: 'filter conditions',
            },
          },
      };

      const grid = {
        oldSort: ['name', 'code'],
        oldFilter: {
          filter: 'filter conditions',
        },

        contentNode: 'content_node',
        row(focusedNode) {
          return { id: `${focusedNode}_refresh_id` };
        },

        refresh: sinon.spy(),
      };

      const showMessage = sinon.spy();

      refreshGrid(grid, context, showMessage);

      grid.context.should.deep.equal({
        part1: 'part1',
        part2: 'part2',
        refreshParams:
          {
            selectKey: ['current'],
            sort: ['name', 'code'],
            filter: {
              filter: 'filter conditions',
            },
          },
      });

      grid.refreshId.should.equal('content_node_refresh_id');

      grid.firstLoading.should.be.false;

      grid.refresh.should.have.been.calledOnceWith({ keepScrollPosition: true });

      showMessage.should.have.not.been.called;
    });

    it('selectKey = current, sort or filter changed', () => {
      const context = {
        part1: 'part1',
        part2: 'part2',
        refreshParams:
          {
            selectKey: ['current'],
            sort: ['name', 'code'],
            filter: {
              filter: 'filter conditions',
            },
          },
      };

      const grid = {
        oldSort: ['name', 'code'],
        oldFilter: {
          filter: 'filter conditions 2',
        },

        selection: {
          code1: true,
          code2: false,
          code3: true,
        },

        _columns: [
          { id: 'name' },
          { id: 'rnum' },
          { id: 'code' }],

        refresh: sinon.spy(),
        renderHeader: sinon.spy(),
      };

      const showMessage = sinon.spy();

      refreshGrid(grid, context, showMessage);

      grid.context.should.deep.equal({
        part1: 'part1',
        part2: 'part2',
        refreshParams:
          {
            selectKey: ['code1'],
            sort: ['name', 'code'],
            filter: {
              filter: 'filter conditions',
            },
          },
      });

      grid.firstLoading.should.be.true;

      grid.renderHeader.should.have.been.calledOnceWith();

      grid.refresh.should.have.been.calledOnceWith({ keepScrollPosition: false });

      showMessage.should.have.not.been.called;
    });

    it('selectKey = current, sort or filter changed, no selectKey', () => {
      const context = {
        part1: 'part1',
        part2: 'part2',
        refreshParams:
          {
            selectKey: ['current'],
            sort: ['name', 'code'],
            filter: {
              filter: 'filter conditions',
            },
          },
      };

      const grid = {
        oldSort: ['name', 'code'],
        oldFilter: {
          filter: 'filter conditions 2',
        },

        selection: {},
      };

      const showMessage = sinon.spy();

      refreshGrid(grid, context, showMessage);

      showMessage.should.have.been.calledOnce;
    });

    it('selectKey != current', () => {
      const context = {
        part1: 'part1',
        part2: 'part2',
        refreshParams:
          {
            sort: ['name', 'code'],
            filter: {
              filter: 'filter conditions',
            },
          },
      };

      const grid = {
        oldSort: ['name', 'code'],
        oldFilter: {
          filter: 'filter conditions 2',
        },

        selection: {
          code1: true,
          code2: false,
          code3: true,
        },

        _columns: [
          { id: 'name' },
          { id: 'rnum' },
          { id: 'code' }],

        refresh: sinon.spy(),
        renderHeader: sinon.spy(),
      };

      const showMessage = sinon.spy();

      refreshGrid(grid, context, showMessage);

      grid.context.should.deep.equal({
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

      grid.firstLoading.should.be.true;

      grid.renderHeader.should.have.been.calledOnceWith();

      grid.refresh.should.have.been.calledOnceWith({ keepScrollPosition: false });

      showMessage.should.have.not.been.called;
    });
  });

  describe('setColumnsVisibility(grid, columns)', () => {
    it('setColumnsVisibility(grid, columns)', () => {
      const columns = [
        {
          id: 'name',
          visible: true,
        },
        {
          id: 'rnum',
          visible: false,
        },
        {
          id: 'code',
          visible: true,
        }];

      const grid = {
        toggleColumnHiddenState: sinon.spy(),
      };

      setColumnsVisibility(grid, columns);

      grid.toggleColumnHiddenState.withArgs('name', false).should.have.been.calledOnce;
      grid.toggleColumnHiddenState.withArgs('rnum', true).should.have.been.calledOnce;
      grid.toggleColumnHiddenState.withArgs('code', false).should.have.been.calledOnce;
    });
  });
});
