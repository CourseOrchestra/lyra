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


import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import axios from 'axios';

import {
  getFullContext, getMetadataUrl, getScrollbackUrl, getShowMessageFunction,
} from './util';
import createGrid from './grid';
import { refreshGrid, scrollBack, setColumnsVisibility } from './grid2';


export const lyraGridSocket = new Client({
  webSocketFactory() {
    return new SockJS(getScrollbackUrl());
  },

  onConnect() {
    this.subscribe('/position', this.scrollBackGrid, { id: 'lyra-grid' });
  },

  scrollBackGrid(scrollBackParams) {
    const params = JSON.parse(scrollBackParams.body);
    const div = document.querySelector(`div#${params.dgridId}`);
    if (div && div.parentElement && div.parentElement.__vue__) {
      const { grid } = div.parentElement.__vue__;
      scrollBack(grid, params.position);
    }
  },

  logRawCommunication: true,

  debug: (str) => {
    console.log(str); // eslint-disable-line no-console
  },
});
lyraGridSocket.activate();


export default {
  name: 'LyraGrid',

  props: {
    formclass: {
      type: String,
      default: null,
    },
    instanceid: {
      type: String,
      default: null,
    },
    context: {
      type: Object,
      default: null,
    },
  },

  data() {
    return {
      header: '',
      footer: '',
    };
  },

  computed: {
    gridDivId() {
      return `${this.formclass}.${this.instanceid}`.replace(/\./g, '-');
    },
  },

  grid: null,

  created() {
    this.$on('refresh', (context) => {
      refreshGrid(this.grid, context, this.showMessage);
    });
    this.$on('set-columns-visibility', (columns) => {
      setColumnsVisibility(this.grid, columns);
    });
  },

  mounted() {
    axios.post(getMetadataUrl(), {
      formClass: this.formclass,
      instanceId: this.instanceid,
      clientParams: getFullContext(this.context),
    }, {
      headers: {
        'Content-Type': 'application/json',
      },
    })
      .then((response) => {
        const metadata = response.data;

        const div = this.$el.querySelector(`#${this.gridDivId}`);
        try {
          div.style = `width:${metadata.common.gridWidth}; height:${metadata.common.gridHeight};`;
        } catch (e) {
          return;
        }

        this.grid = createGrid(
          metadata,
          this.gridDivId,
          this.formclass,
          this.instanceid,
          this.context,
          this.showMessage,
          this.emitEvent,
          this.setLabels,
        );
      })
      .catch((error) => {
        this.showMessage(error);
      });
  },

  methods: {
    setLabels(header, footer) {
      this.header = header;
      this.footer = footer;
    },
    emitEvent(eventType, obj) {
      this.$emit(eventType, this.formclass, this.instanceid, obj);
    },
    showMessage(message) {
      const showMessageFunction = getShowMessageFunction();
      if (showMessageFunction) {
        showMessageFunction(this, message);
      }
      throw new Error(message);
    },
  },


};


</script>
