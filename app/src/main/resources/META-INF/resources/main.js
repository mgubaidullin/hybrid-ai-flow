import Vue from '/js/vue.esm.browser.min.js'
import { MainTemplate } from './templates/main-template.js'
import { Data } from './components/data.js'

// Router
Vue.use(VueRouter);
const router = new VueRouter({
  routes: [
    { path: '/', redirect: "/data" },
    { path: '/data', component: Data, name: "Images" },
  ]
})

// Application
var client = new Vue({
  el: '#app',
  router,
  template: MainTemplate,
  mounted: function () {
  }
})