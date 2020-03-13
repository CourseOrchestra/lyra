import {
  getDataUrl,
  getFullContext,
  getMetadataUrl,
  getScrollbackUrl,
  getSelectObject,
  getShowMessageFunction,
  getTitle,
  isEqual,
} from '../../src/util';


describe('utils.js', () => {
  afterEach(() => {
    window.getLyraConfig = null;
  });

  describe('getMetadataUrl()', () => {
    it('lyraConfig is not defined', () => {
      expect(getMetadataUrl())
        .to
        .equal('/lyra/metadata');
    });
    it('lyraConfig is defined, baseUrl is not defined', () => {
      window.getLyraConfig = function getLyraConfig() {
        return {};
      };
      expect(getMetadataUrl())
        .to
        .equal('/lyra/metadata');
    });
    it('lyraConfig is defined, baseUrl is defined', () => {
      window.getLyraConfig = function getLyraConfig() {
        return {
          baseUrl: 'http://localhost:8081',
        };
      };
      expect(getMetadataUrl())
        .to
        .equal('http://localhost:8081/lyra/metadata');
    });
  });

  describe('getDataUrl()', () => {
    it('lyraConfig is not defined', () => {
      expect(getDataUrl())
        .to
        .equal('/lyra/data');
    });
    it('lyraConfig is defined, baseUrl is not defined', () => {
      window.getLyraConfig = function getLyraConfig() {
        return {};
      };
      expect(getDataUrl())
        .to
        .equal('/lyra/data');
    });
    it('lyraConfig is defined, baseUrl is defined', () => {
      window.getLyraConfig = function getLyraConfig() {
        return {
          baseUrl: 'http://localhost:8081',
        };
      };
      expect(getDataUrl())
        .to
        .equal('http://localhost:8081/lyra/data');
    });
  });

  describe('getScrollbackUrl()', () => {
    it('lyraConfig is not defined', () => {
      expect(getScrollbackUrl())
        .to
        .equal('/lyra/scrollback');
    });
    it('lyraConfig is defined, baseUrl is not defined, baseUrlScrollback is not defined', () => {
      window.getLyraConfig = function getLyraConfig() {
        return {};
      };
      expect(getScrollbackUrl())
        .to
        .equal('/lyra/scrollback');
    });
    it('lyraConfig is defined, baseUrl is defined, baseUrlScrollback is not defined', () => {
      window.getLyraConfig = function getLyraConfig() {
        return {
          baseUrl: 'http://localhost:8081',
        };
      };
      expect(getScrollbackUrl())
        .to
        .equal('http://localhost:8081/lyra/scrollback');
    });
    it('lyraConfig is defined, baseUrl is defined, baseUrlScrollback is defined', () => {
      window.getLyraConfig = function getLyraConfig() {
        return {
          baseUrl: 'http://localhost:8081',
          baseUrlScrollback: 'http://localhost:8088',
        };
      };
      expect(getScrollbackUrl())
        .to
        .equal('http://localhost:8088/lyra/scrollback');
    });
  });

  describe('getShowMessageFunction()', () => {
    it('lyraConfig is not defined', () => {
      expect(getShowMessageFunction()).to.be.null;
    });

    it('lyraConfig is defined, showMessageFunction is not defined', () => {
      window.getLyraConfig = function getLyraConfig() {
        return {};
      };
      expect(getShowMessageFunction()).to.be.null;
    });
    it('lyraConfig is defined, showMessageFunction is defined', () => {
      window.getLyraConfig = function getLyraConfig() {
        return {
          showMessageFunction() {
          },
        };
      };
      expect(getShowMessageFunction()).to.be.not.null;
    });
  });


  describe('getTitle(title)', () => {
    it('title is not defined', () => {
      expect(getTitle()).to.be.undefined;
    });
    it('title is defined', () => {
      expect(getTitle('I&amp;t&lt; i&lt;s &gt;a s&amp;tr&gt;in&amp;g'))
        .to
        .equal('I&t< i<s >a s&tr>in&g');
    });
  });

  describe('getSelectObject(row, col, selection)', () => {
    it('getSelectObject(row, col, selection)', () => {
      const row = {
        id: 'code5',
        data: { name: 'NAME' },
      };
      const col = { id: 'field1' };
      const selection = ['code1', 'code5'];

      expect(getSelectObject(row, col, selection))
        .to
        .deep
        .equal({
          currentColId: 'field1',
          currentRowId: 'code5',
          currentRowData: { name: 'NAME' },
          selection: ['code1', 'code5'],
        });
    });
  });

  describe('getFullContext(context, sort, filter))', () => {
    it('context is not defined', () => {
      expect(getFullContext())
        .to
        .deep
        .equal({
          refreshParams: {
            sort: [],
          },
        });
    });

    it('context is defined, context.refreshParams is not defined', () => {
      expect(getFullContext({
        part1: 'part1',
        part2: 'part2',
      }))
        .to
        .deep
        .equal({
          part1: 'part1',
          part2: 'part2',
          refreshParams: {
            sort: [],
          },
        });
    });

    it('context.refreshParams.sort is defined', () => {
      const sort = ['oldSort'];
      expect(getFullContext({
        refreshParams: {
          sort: ['sort'],
        },
      }, sort))
        .to
        .deep
        .equal({
          refreshParams: {
            sort: ['sort'],
          },
        });
    });

    it('context.refreshParams.sort is not defined', () => {
      const sort = ['oldSort'];
      expect(getFullContext({
        refreshParams: {},
      }, sort))
        .to
        .deep
        .equal({
          refreshParams: {
            sort: ['oldSort'],
          },
        });
    });

    it('context.refreshParams.filter is defined', () => {
      const sort = 'oldSort';
      const filter = 'oldFilter';
      expect(getFullContext({
        refreshParams: {
          filter: 'filter',
        },
      }, sort, filter))
        .to
        .deep
        .equal({
          refreshParams: {
            sort: 'oldSort',
            filter: 'filter',
          },
        });
    });

    it('context.refreshParams.filter.current = true', () => {
      const sort = 'oldSort';
      const filter = 'oldFilter';
      expect(getFullContext({
        refreshParams: { filter: { current: true } },
      }, sort, filter))
        .to
        .deep
        .equal({
          refreshParams: {
            sort: 'oldSort',
            filter: 'oldFilter',
          },
        });
    });

    it('context.refreshParams.selectKey is []', () => {
      const sort = 'oldSort';
      const filter = 'oldFilter';
      expect(getFullContext({
        refreshParams: {
          selectKey: [],
        },
      }, sort, filter))
        .to
        .deep
        .equal({
          refreshParams: {
            sort: 'oldSort',
            selectKey: null,
          },
        });
    });

    it('context.refreshParams.selectKey is not []', () => {
      const sort = 'oldSort';
      const filter = 'oldFilter';
      expect(getFullContext({
        refreshParams: {
          selectKey: ['code1'],
        },
      }, sort, filter))
        .to
        .deep
        .equal({
          refreshParams: {
            sort: 'oldSort',
            selectKey: ['code1'],
          },
        });
    });
  });


  describe('isEqual(obj1, obj2)', () => {
    it('sort is equal', () => {
      const obj1 = ['name', 'code'];
      const obj2 = ['name', 'code'];
      expect(isEqual(obj1, obj2)).to.be.true;
    });
    it('sort is not equal', () => {
      const obj1 = ['name', 'code'];
      const obj2 = ['name', 'code2'];
      expect(isEqual(obj1, obj2)).to.be.false;
    });

    it('filter is equal', () => {
      const obj1 = {
        cond1: 'cond1',
        cond2: 'cond2',
      };
      const obj2 = {
        cond2: 'cond2',
        cond1: 'cond1',
      };
      expect(isEqual(obj1, obj2)).to.be.true;
    });
    it('filter is not equal', () => {
      const obj1 = {
        cond1: 'cond1',
        cond2: 'cond2',
      };
      const obj2 = {
        cond2: 'cond22',
        cond1: 'cond1',
      };
      expect(isEqual(obj1, obj2)).to.be.false;
    });
  });
});
