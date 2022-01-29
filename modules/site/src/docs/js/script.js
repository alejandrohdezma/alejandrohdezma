// highlight-js
hljs.configure({ ignoreUnescapedHTML: true });
hljs.highlightAll();

// progress-bar

window.addEventListener("scroll", e => {
    let limit = Math.max(document.body.scrollHeight, document.body.offsetHeight,
        document.documentElement.clientHeight, document.documentElement.scrollHeight,
        document.documentElement.offsetHeight);

    let max = limit - window.innerHeight;

    let progress = window.pageYOffset / max * 100;

    progress = Math.min(100, Math.max(0, progress));

    document.getElementById("progress-display").style.width = `${progress}%`
})

// reading-time

const lang = document.documentElement.lang

const text = document.getElementsByTagName("article")[0].innerText;
const time = Math.ceil(text.trim().split(/\s+/).length / 225);

document.getElementById("time").innerText = lang == 'es' ? `${time} minutos` : `${time} minutes`;