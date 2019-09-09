import query from 'dojo/query';
import lang from 'dojo/_base/lang';
import Grid from 'dgrid/OnDemandGrid';
import ColumnResizer from 'dgrid/extensions/ColumnResizer';
import ColumnHider from 'dgrid/extensions/ColumnHider';
import ColumnReorder from 'dgrid/extensions/ColumnReorder';
import Selection from 'dgrid/Selection';
import Keyboard from 'dgrid/Keyboard';
import Declare from 'dojo/_base/declare';
import domConstruct from 'dojo/dom-construct';

import { getFullContext, getSelectObject, getTitle } from './util';
import { getSelection, setExternalSorting } from './grid2';
import createStore from './store';


export default function createGrid(
  metadata, gridDivId,
  formClass, instanceId, aContext,
  showMessage, emitEvent, setLabels,
) {
  const context = getFullContext(aContext);

  const columns = [];
  const columnsInfo = [];
  for (let k = 0; k < metadata.columns.length; k += 1) {
    const column = {};

    column.id = metadata.columns[k].id;
    column.field = metadata.columns[k].id;
    column.hidden = !metadata.columns[k].visible;
    column.sortable = metadata.columns[k].sortable;
    // column["unhidable"] = true;
    column.label = metadata.columns[k].caption;
    column.sortingAvailable = metadata.columns[k].sortingAvailable;
    column.className = metadata.columns[k].cssClassName;
    columnsInfo.push({ ...column });


    column.renderCell = function actionRenderCell(object, aValue /* node, options */) {
      let value = aValue;
      if (!value) {
        value = '';
      }

      const div = document.createElement('div');
      div.innerHTML = value;
      div.title = value;
      div.title = getTitle(div.title);

      return div;
    };


    column.renderHeaderCell = function actionRenderCell(/* node */) {
      const div = document.createElement('div');
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
  emitEvent('columns-info', columnsInfo);


  setExternalSorting(columns, context.refreshParams.sort);


  const localizedParams = {
    loadingMessage: 'Загрузка...',
    noDataMessage: 'Нет записей',
  };
  const initOldValue = 'D13k82F9g7';
  const grid = new Declare(
    [Grid, ColumnResizer, ColumnHider, ColumnReorder, Keyboard, Selection],
  )({
    columns,

    minRowsPerPage: metadata.common.limit,
    maxRowsPerPage: metadata.common.limit,
    bufferRows: 0,
    farOffRemoval: 0,
    pagingDelay: 50,

    selectionMode: 'extended',

    allowTextSelection: !!metadata.common.allowTextSelection,
    showHeader: !!metadata.common.visibleColumnsHeader,
    loadingMessage: localizedParams.loadingMessage,
    noDataMessage: localizedParams.noDataMessage,

    deselectOnRefresh: false,

    keepScrollPosition: false,

    renderRow(object) {
      const rowElement = Grid.prototype.renderRow.call(this, object);
      if (object.recordProperties && object.recordProperties.rowstyle) {
        rowElement.className = `${rowElement.className} ${object.recordProperties.rowstyle} `;
      }
      return rowElement;
    },

    backScroll: false,
    resScroll: null,

    needBackScroll: true,

    dgridOldPosition: 0,
    limit: metadata.common.limit,

    firstLoading: true,

    oldSort: initOldValue,
    oldFilter: initOldValue,

    formClass,
    instanceId,
    context,

    showFooter: !!metadata.common.summaryRow,
    summary: metadata.common.summaryRow,

    buildRendering() {
      ColumnResizer.prototype.buildRendering.call(this);
      this.summaryAreaNode = domConstruct.create('div', {
        className: 'summary-row',
        role: 'row',
        style: { overflow: 'hidden' },
      }, this.footerNode);
      const areaNode = this.summaryAreaNode;

      this.on('scroll', lang.hitch(this, function func() {
        areaNode.scrollLeft = this.getScrollPosition().x;
      }));
    },

    _updateColumns() {
      Grid.prototype._updateColumns.call(this);
      if (this.summary) {
        this.setSummary(this.summary);
      }
    },

    _renderSummaryCell(item, cell, column) {
      const value = item[column.field] || '';
      cell.appendChild(document.createTextNode(value));
    },

    setSummary(data) {
      let tableNode = this.summaryTableNode;

      this.summary = data;

      if (tableNode) {
        domConstruct.destroy(tableNode);
      }

      this.summaryTableNode = this.createRowCells('td',
        lang.hitch(this, '_renderSummaryCell', data));
      tableNode = this.summaryTableNode;
      this.summaryAreaNode.appendChild(tableNode);

      if (this._started) {
        this.resize();
      }
    },

    adjustFooterCellsWidths() {
      if (!this._resizedColumns) {
        const colNodes = query('.dgrid-cell', this.headerNode);

        const colWidths = colNodes.map((colNode) => colNode.offsetWidth);

        colNodes.forEach(function func(colNode, i) {
          this.resizeColumnWidth(colNode.columnId, colWidths[i]);
        }, this);
      }

      const obj = this._getResizedColumnWidths();
      const lastCol = obj.lastColId;

      this.resizeColumnWidth(lastCol, 'auto');
    },


  }, gridDivId);


  for (let k = 0; k < metadata.columns.length; k += 1) {
    grid.styleColumn(metadata.columns[k].id, metadata.columns[k].cssStyle);
  }

  if (grid.summary) {
    grid.setSummary(grid.summary);
    grid.adjustFooterCellsWidths();
  }


  grid.on('dgrid-columnreorder', (event) => {
    setTimeout(() => {
      if (event.grid.summary) {
        event.grid.adjustFooterCellsWidths();
      }
    });
  });


  grid.on('dgrid-select', (event) => {
    const row = grid.row(event.grid._focusedNode);
    const col = grid.column(event.grid._focusedNode);
    if (row && col) {
      emitEvent('select', getSelectObject(row, col, getSelection(grid)));
    }
  });

  grid.on('.dgrid-row:click', (event) => {
    const row = grid.row(event);
    const col = grid.column(event);
    if (row && col) {
      emitEvent('click', getSelectObject(row, col, getSelection(grid)));
    }
  });
  grid.on('.dgrid-row:dblclick', (event) => {
    const row = grid.row(event);
    const col = grid.column(event);
    if (row && col) {
      emitEvent('dblclick', getSelectObject(row, col, getSelection(grid)));
    }
  });


  grid.on('dgrid-sort', (event) => {
    const sort = [];
    const desc = event.sort[0].descending ? ' desc' : '';

    sort.push(event.sort[0].property + desc);

    if (metadata.common.primaryKey) {
      for (let n = 0; n < metadata.common.primaryKey.length; n += 1) {
        if (!((n === 0) && (event.sort[0].property === metadata.common.primaryKey[n]))) {
          sort.push(metadata.common.primaryKey[n] + desc);
        }
      }
    }


    grid.context.refreshParams.sort = sort;
    grid.context.refreshParams.selectKey = null;


    setExternalSorting(grid._columns, sort);
    grid.renderHeader();

    grid.firstLoading = true;
  });


  grid.on('dgrid-refresh-complete', () => {
    grid.refreshId = null;

    if (grid.firstLoading && grid.dgridNewPosition) {
      let pos = parseInt(grid.dgridNewPosition, 10);
      pos *= grid.rowHeight;
      grid.backScroll = true;
      grid.needBackScroll = false;
      grid.scrollTo({
        x: 0,
        y: pos,
      });
      grid.clearSelection();
      grid.select(grid.row(grid.dgridNewPositionId));
      grid.row(grid.dgridNewPositionId)
        .element
        .scrollIntoView({
          block: 'start',
          behavior: 'smooth',
        });
      grid.dgridNewPosition = null;
      grid.dgridNewPositionId = null;
    }

    grid.firstLoading = false;
  });


  const store = createStore(grid, showMessage, setLabels);
  grid.set('collection', store);


  return grid;
}
