document.addEventListener("DOMContentLoaded", () => {
  const marquee = document.querySelector("h1");
  if (!marquee) return;

  // Оборачиваем текст в отдельный внутренний контейнер для анимации
  const textContent = marquee.innerHTML;
  marquee.innerHTML = `<span class="marquee-inner" style="display: inline-block; padding-left: 100%;">${textContent}</span>`;

  const innerSpan = marquee.querySelector(".marquee-inner");

  // Создаем динамический CSS-класс для анимации бегущей строки
  const style = document.createElement("style");
  style.innerHTML = `
    @keyframes marqueeAnimation {
      0% { transform: translate3d(0, 0, 0); }
      100% { transform: translate3d(-100%, 0, 0); }
    }
    .marquee-inner {
      animation: marqueeAnimation 10s linear infinite;
    }
  `;
  document.head.appendChild(style);
});