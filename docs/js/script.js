// scroll-to-top
const button = document.createElement('button')
button.id = 'scroll-to-top'
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

window.addEventListener('scroll', e => {
    const offset = document.documentElement.scrollTop
    const button = document.getElementById('scroll-to-top')
    button.style.display = offset >= 500 ? "inline-block" : "none"
})

// progress-bar

const progressWrapper = document.createElement('div')
progressWrapper.className = "progress-wrapper"
const progressDisplay = document.createElement('div')
progressDisplay.id = "progress-display"
progressWrapper.appendChild(progressDisplay)

document.body.appendChild(progressWrapper)

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

const text = document.getElementsByTagName("article")[0].innerText;
const wpm = 225;
const words = text.trim().split(/\s+/).length;
const time = Math.ceil(words / wpm);
document.getElementById("time").innerText = time;