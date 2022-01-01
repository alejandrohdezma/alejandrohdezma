function plugin(hook, vm) {
    hook.doneEach(() => {
        const a = Docsify.dom.find('a.github-corner')
        let emoji = name => `<img class="emoji" src="https://github.githubassets.com/images/icons/emoji/${name}.png">`

        if (document.documentElement.lang == 'es') {
            a.href = `${window.location.origin}/#${vm.route.path.slice(3)}`
            a.innerHTML = `${emoji('es')} <i class="fas fa-arrow-right"></i> ${emoji('us')}`
            a.title = 'Show english version'
        } else {
            a.href = `${window.location.origin}/#/es${vm.route.path}`
            a.innerHTML = `${emoji('us')} <i class="fas fa-arrow-right"></i> ${emoji('es')}`
            a.title = 'Ver la versión en español'
        }
    })
}

window.$docsify.plugins = [].concat(plugin, window.$docsify.plugins)