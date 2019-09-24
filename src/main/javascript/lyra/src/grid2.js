import { getFullContext, isEqual } from './util';


export function scrollBack(aGrid, position) {
  const grid = aGrid;
  if (grid.needBackScroll) {
    let pos = position;
    pos *= grid.rowHeight;
    pos = pos
      + grid.getScrollPosition().y
      - Math.floor(grid.getScrollPosition().y / grid.rowHeight) * grid.rowHeight;
    pos = Math.round(pos);
    if (pos >= 0) {
      grid.backScroll = true;
      grid.scrollTo({
        x: 0,
        y: pos,
      });
    }
  }
}

export function getSelection(grid) {
  const selection = [];
  Object.keys(grid.selection)
    .forEach((key) => {
      if (grid.selection[key]) {
        selection.push(key);
      }
    });
  return selection;
}


export function setExternalSorting(aColumns, sort) {
  const columns = aColumns;

  for (let n = 0; n < columns.length; n += 1) {
    columns[n].sortingPic = null;
  }

  let desc = false;
  for (let m = 0; m < sort.length; m += 1) {
    if ((m === 0) && (sort[m].toLowerCase()
      .indexOf(' desc') > -1)) {
      desc = true;
    }

    let sortName = sort[m].substring(0, sort[m].indexOf(' '));
    if (sortName === '') {
      sortName = sort[m];
    }

    for (let n = 0; n < columns.length; n += 1) {
      if (columns[n].id === sortName) {
        if (desc) {
          columns[n].sortingPic = 'd';
        } else {
          columns[n].sortingPic = 'a';
        }

        columns[n].sortingPic += (m + 1);

        break;
      }
    }
  }
}


export function refreshGrid(aGrid, aContext, showMessage) {
  const grid = aGrid;
  const context = getFullContext(aContext, grid.oldSort, grid.oldFilter);
  if (context.refreshParams.selectKey && context.refreshParams.selectKey[0] === 'current') {
    const { sort } = context.refreshParams;
    const { filter } = context.refreshParams;
    if (isEqual(sort, grid.oldSort) && isEqual(filter, grid.oldFilter)) {
      grid.context = context;
      const focusedNode = grid._focusedNode || grid.contentNode;
      grid.refreshId = grid.row(focusedNode).id;
      grid.firstLoading = false;
      grid.refresh({ keepScrollPosition: true });
    } else {
      const selectKey = getSelection(grid)[0];

      if (!selectKey) {
        showMessage('Отсутствует выделенная запись. Выполнение операции невозможно.');
        return;
      }

      context.refreshParams.selectKey = [selectKey];
      grid.context = context;

      setExternalSorting(grid._columns, sort);
      grid.renderHeader();

      grid.firstLoading = true;
      grid.refresh({ keepScrollPosition: false });
    }
  } else {
    grid.context = context;

    const { sort } = grid.context.refreshParams;
    setExternalSorting(grid._columns, sort);
    grid.renderHeader();

    grid.firstLoading = true;
    grid.refresh({ keepScrollPosition: false });
  }
}

export function setColumnsVisibility(grid, columns) {
  for (let n = 0; n < columns.length; n += 1) {
    grid.toggleColumnHiddenState(columns[n].id, !columns[n].visible);
  }
}
