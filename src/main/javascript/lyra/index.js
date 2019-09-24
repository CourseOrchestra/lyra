import LyraGrid from './src/LyraGrid.vue';

export const Lyra = {
  install(Vue) {
    Vue.component('lyra-grid', LyraGrid);
  },
};

export default LyraGrid;
