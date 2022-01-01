function plugin(hook, vm) {
    hook.beforeEach(html =>
        html +
        '\n\n' +
        '<div class="updated_at"><strong><i class="far fa-calendar"></i> ' +
        (document.documentElement.lang == 'es' ? 'Actualizado' : 'Updated') +
        ': {docsify-updated}</strong></div>'
    )
}

window.$docsify.plugins = [].concat(plugin, window.$docsify.plugins)