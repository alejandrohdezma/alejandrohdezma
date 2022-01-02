function scrollToTop(hook, vm) {
    hook.mounted(() => {
        const button = document.createElement('button')
        button.className = 'scroll-to-top'
        button.role = 'button'

        const arrow = document.createElement('i')
        arrow.className = "fas fa-arrow-up"

        button.appendChild(arrow)
        button.onclick = event => {
            event.stopPropagation()
            const step = window.scrollY / 15

            const scroll = () => {
                window.scrollTo(0, window.scrollY - step)
                if (window.scrollY > 0) {
                    setTimeout(scroll, 15)
                }
            }

            scroll()
        }

        document.body.appendChild(button)
    })

    hook.mounted(() => {
        window.addEventListener('scroll', e => {
            const offset = window.document.documentElement.scrollTop
            const button = Docsify.dom.find('button.scroll-to-top')
            button.style.display = offset >= 500 ? "inline-block" : "none"
        })
    })
}

window.$docsify.plugins = [].concat(scrollToTop, window.$docsify.plugins)