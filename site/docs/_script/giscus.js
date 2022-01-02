function giscus(hook, vm) {
    hook.doneEach(() => {
        if (!vm.route.file.includes("README") && !window.origin.includes("localhost")) {
            const article = Docsify.dom.find('article#main')

            const script = document.createElement('script')

            script.async = 'async'
            script.src = "https://giscus.app/client.js"
            script.setAttribute('data-repo', "alejandrohdezma/alejandrohdezma")
            script.setAttribute('data-repo-id', "MDEwOlJlcG9zaXRvcnkzMTY5NDkzOTY=")
            script.setAttribute('data-category', "Blog")
            script.setAttribute('data-category-id', "DIC_kwDOEuRDlM4CAfTC")
            script.setAttribute('data-mapping', "title")
            script.setAttribute('data-reactions-enabled', "1")
            script.setAttribute('data-emit-metadata', "0")
            script.setAttribute('data-theme', "light")
            script.setAttribute('data-lang', document.documentElement.lang)
            script.setAttribute('crossorigin', "anonymous")

            article.appendChild(script)
        }
    })
}

window.$docsify.plugins = [].concat(giscus, window.$docsify.plugins)