function header(hook, vm) {
    let wordsCount

    hook.beforeEach(content => {
        wordsCount = content.match(/([\u0800-\u4e00]+?|[\u4e00-\u9fa5]+?|[a-zA-Z0-9]+)/g).length
        return content
    })

    hook.beforeEach((html, next) => {
        if (vm.route.file.includes("README")) next(html)
        else {
            const div = `<div style="margin-top:10px">
                <span class="updated-at">
                    <i class="far fa-calendar"></i>
                    ${(document.documentElement.lang == 'es' ? 'Actualizado' : 'Updated')}
                    : {docsify-updated}
                </span>
                <span class="reading-count">
                    ${wordsCount + (document.documentElement.lang == 'es' ? ' palabras' : ' words')}
                    &nbsp; | &nbsp;${Math.ceil(wordsCount / 400) + (document.documentElement.lang == 'es' ? ' minutos' : ' minutes')}
                </span>
                <div style="clear: both"></div>
            </div>`

            next(`${div}\n\n${html}`)
        }
    })
}

window.$docsify.plugins = [].concat(header, window.$docsify.plugins)