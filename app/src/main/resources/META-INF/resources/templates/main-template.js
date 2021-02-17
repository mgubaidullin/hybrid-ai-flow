const MainTemplate = `
<div class="pf-c-page" id="page-default-nav-example">
    <a class="pf-c-skip-to-content pf-c-button pf-m-primary" href="#main-content-page-default-nav-example">Skip to
        content</a>
    <header role="banner" class="pf-c-page__header">
        <div class="pf-c-page__header-brand">
            <img class="logo" src="/img/pf-icon-running.svg" alt="" />
            <a class="pf-c-page__header-brand-link">
                <h1 class="header">HYBRID AI FLOW</h1>
            </a>
        </div>
        <div class="pf-c-page__header-tools">
            <img class="pf-c-avatar" src="/img/img_avatar.svg" alt="Avatar image"/>
        </div>
    </header>
    <div class="pf-c-page__sidebar pf-m-dark" id="sidebar">
        <div class="pf-c-page__sidebar-body">
            <nav class="pf-c-nav pf-m-dark" id="page-default-nav-example-primary-nav" aria-label="Global">
                <ul class="pf-c-nav__list">
                    <li class="pf-c-nav__item">
                        <router-link to="/data" v-bind:class="[$route.path === '/data' ? 'pf-m-current' : '', 'pf-c-nav__link']">Data</router-link>
                    </li>
                    </li>
                </ul>
            </nav>
        </div>
    </div>

    <main role="main" class="pf-c-page__main" tabindex="-1">
        <router-view></router-view>
    </main>
</div>
`

export { MainTemplate }