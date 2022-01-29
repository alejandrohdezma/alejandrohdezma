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

// clipboard-js

document.querySelectorAll("pre").forEach(pre => {
    pre.addEventListener("mouseover", () => {
        pre.childNodes[0].style.display = "block";
    });
    pre.addEventListener("mouseout", () => {
        pre.childNodes[0].style.display = "none";
    }) 
});

const clipboard = new ClipboardJS('pre button', { target: trigger => trigger.nextElementSibling });

clipboard.on('success', event => {
    event.trigger.childNodes[0].className = "fas fa-check"

    setTimeout(() => { event.trigger.childNodes[0].className = "far fa-copy" }, 2000);

    event.clearSelection();
});