import { DataTemplate } from "../templates/data-template.js";
import Vue from '/js/vue.esm.browser.min.js'

const Data = Vue.component('Data', {
  data: function () {
    return {
      rows: [],
      showSpinner: false,
      file: null
    }
  },
  mounted: function () {
    this.getData();
  },
  methods: {
    getData: function (event) {
      this.showSpinner = true;
      axios.get('/image').then(response => {
        this.rows = response.data;
        this.showSpinner = false;
      });
    },
    submitFile(){
        var formData = new FormData();
        formData.append('file', this.file);
        formData.append('filename', this.file.name);
        formData.append('mimetype', this.file.type);
        axios.post('/image', formData, {headers: {'Content-Type': 'multipart/form-data' }})
            .then(response => {
               this.getData();
             });
    },
    handleFileUpload(){
        this.file = this.$refs.file.files[0];
    },
  },
  template: DataTemplate
})

export { Data }