import diff from 'deep-diff';

export function getMetadataUrl() {
  const url = '/lyra/metadata';
  const lyraConfig = window.getLyraConfig ? window.getLyraConfig() : null;
  return lyraConfig && lyraConfig.baseUrl ? lyraConfig.baseUrl + url : url;
}

export function getDataUrl() {
  const url = '/lyra/data';
  const lyraConfig = window.getLyraConfig ? window.getLyraConfig() : null;
  return lyraConfig && lyraConfig.baseUrl ? lyraConfig.baseUrl + url : url;
}

export function getScrollbackUrl() {
  let url = '/lyra/scrollback';
  const lyraConfig = window.getLyraConfig ? window.getLyraConfig() : null;
  if (lyraConfig) {
    if (lyraConfig.baseUrlScrollback) {
      url = lyraConfig.baseUrlScrollback + url;
    } else if (lyraConfig.baseUrl) {
      url = lyraConfig.baseUrl + url;
    }
  }
  return url;
}

export function getShowMessageFunction() {
  const lyraConfig = window.getLyraConfig ? window.getLyraConfig() : null;
  return lyraConfig && lyraConfig.showMessageFunction ? lyraConfig.showMessageFunction : null;
}


export function getTitle(title) {
  let res = title;
  if (res) {
    res = res.replace(/&lt;/g, '<');
    res = res.replace(/&gt;/g, '>');
    res = res.replace(/&amp;/g, '&');
  }
  return res;
}


export function getSelectObject(row, col, selection) {
  return {
    currentColId: col.id,
    currentRowId: row.id,
    currentRowData: row.data,
    selection,
  };
}


export function getFullContext(aContext, sort, filter) {
  let context = aContext;

  if (!context) {
    context = { refreshParams: {} };
  }
  if (!context.refreshParams) {
    context.refreshParams = {};
  }

  if (!context.refreshParams.sort) {
    context.refreshParams.sort = sort || [];
  }

  if (context.refreshParams.filter && context.refreshParams.filter.current) {
    context.refreshParams.filter = filter;
  }

  if (context.refreshParams.selectKey && context.refreshParams.selectKey.length === 0) {
    context.refreshParams.selectKey = null;
  }

  return context;
}

export function isEqual(obj1, obj2) {
  return !diff(obj1, obj2);
}
