function progressBar(hook, vm) {
    hook.mounted(() => {
        const progressWrapper = document.createElement('div')
        progressWrapper.className = "progress-wrapper"
        const progressDisplay = document.createElement('div')
        progressDisplay.id = "progress-display"
        progressWrapper.appendChild(progressDisplay)

        Docsify.dom.body.appendChild(progressWrapper)
    })

    hook.ready(function() {
        window.addEventListener("scroll", e => {
            let limit = Math.max(document.body.scrollHeight, document.body.offsetHeight,
                document.documentElement.clientHeight, document.documentElement.scrollHeight,
                document.documentElement.offsetHeight);

            let max = limit - window.innerHeight;

            let progress = window.pageYOffset / max * 100;

            progress = Math.min(100, Math.max(0, progress));

            document.getElementById("progress-display").style.width = `${progress}%`
        })
    })
}

window.$docsify.plugins = [].concat(progressBar, window.$docsify.plugins)