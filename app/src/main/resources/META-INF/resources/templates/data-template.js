const DataTemplate = `
<div>
    <section class="pf-c-page__main-section pf-m-light">
        <div class="pf-l-split pf-m-gutter">
            <div class="pf-l-split__item">
                <div class="pf-c-content">
                    <h1>Data</h1>
                </div>
            </div>
            <div class="pf-l-split__item pf-m-fill"></div>
            <div class="pf-l-split__item pf-c-form pf-m-horizontal">
                <div class="pf-c-form__group no-space" style="display: block;">
                    <div class="pf-c-form__group-label">
                        <label class="pf-c-form__label" for="file">
                            <input type="file" id="file" ref="file" class="file" accept=".jpg,.jpeg,.png" v-on:change="handleFileUpload()"/>
                        </label>
                        <button class="pf-c-button pf-m-secondary" type="button" v-on:click="submitFile()">
                            <span class="pf-c-button__icon">
                                <i class="fas fa-upload" aria-hidden="true"></i>
                            </span>
                            <span class="pf-c-button__text">Upload</span>
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </section>
    <section class="pf-c-page__main-section">
        <table class="pf-c-table pf-m-grid-md" role="grid">
            <thead>
            <tr>
                <th class="pf-m-width-40">Filename</th>
                <th>Size</th>
                <th>Last Modified</th>
                <th class="pf-m-width-20">Image</th>
            </tr>
            </thead>
            <tr v-show="rows.length === 0" class="pf-m-height-auto" role="row">
                <td role="cell" colspan="8">
                    <div class="pf-l-bullseye">
                        <div class="pf-c-empty-state pf-m-sm">
                            <div v-show="showSpinner === false" class="pf-c-empty-state__content">
                                <i class="fas fa- fa-search pf-c-empty-state__icon" aria-hidden="true"></i>
                                <h2 class="pf-c-title pf-m-lg">No results found</h2>
                                <div class="pf-c-empty-state__body">No results match the select criteria.</div>
                            </div>
                            <div v-show="showSpinner === true" class="pf-c-empty-state__content">
                                <div class="pf-c-empty-state__icon">
                            <span class="pf-c-spinner" role="progressbar" aria-valuetext="Loading...">
                              <span class="pf-c-spinner__clipper"></span>
                              <span class="pf-c-spinner__lead-ball"></span>
                              <span class="pf-c-spinner__tail-ball"></span>
                            </span>
                                </div>
                            </div>
                        </div>
                    </div>
                </td>
            </tr>
            <tr v-show="rows.length != 0" v-for="row in rows" :key="row.name">
                <td>{{row.name}}</td>
                <td>{{row.size}}</td>
                <td>{{row.lastModified}}</td>
                <td>
                    <img class="table-image" :src="row.url" alt="image"/>
                </td>
            </tr>
        </table>
    </section>
</div>
`

export { DataTemplate }