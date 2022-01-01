function plugin(hook, vm) {
    hook.afterEach((html, next) => {
        if (vm.route.file.includes("README")) next(html)
        else {
            const twitter = '<a target="_blank" href="https://twitter.com/alejandrohdezma"><i class="fab fa-twitter"></i></a>'
            const github = '<a target="_blank" href="https://github.com/alejandrohdezma"><i class="fab fa-github"></i></a>'

            const avatar = '<div class="avatar-wrapper"><img class="avatar" src="https://avatars.githubusercontent.com/u/9027541?v=4"/></div>'

            const footer = document.documentElement.lang == 'es' ?
                `Escrito con <i class="fas fa-heart"></i> por Alejandro Hernández. <br>Encuéntrame en ${twitter} o ${github}.` :
                `Written with <i class="fas fa-heart"></i> by Alejandro Hernández. <br>Find me on ${twitter} or ${github}.`

            next(html + `\n\n<footer>${avatar}<p style="margin: 0">${footer}</p></footer>`)
        }
    })
}

window.$docsify.plugins = [].concat(plugin, window.$docsify.plugins)