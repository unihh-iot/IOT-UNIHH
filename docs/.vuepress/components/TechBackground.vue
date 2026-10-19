<template>
  <div class="unihh-bg" aria-hidden="true">
    <!-- 顶部聚焦光 -->
    <div class="unihh-bg__toplight"></div>
    <!-- 大尺度极光:缓慢漂移 -->
    <div class="unihh-bg__aurora">
      <span class="unihh-blob unihh-blob--1"></span>
      <span class="unihh-blob unihh-blob--2"></span>
      <span class="unihh-blob unihh-blob--3"></span>
    </div>
    <!-- 斜向光束 -->
    <div class="unihh-bg__beam"></div>
    <!-- 细腻噪点:消除渐变色带、增加质感 -->
    <div class="unihh-bg__grain"></div>
  </div>
</template>

<style scoped>
.unihh-bg {
  position: fixed;
  inset: 0;
  z-index: 0;
  overflow: hidden;
  pointer-events: none;
  background:
    radial-gradient(120% 90% at 50% -20%, rgba(79, 124, 255, 0.1) 0%, rgba(79, 124, 255, 0) 60%),
    linear-gradient(180deg, #f6f8ff 0%, #eff3fd 55%, #f3f5fb 100%);
}

html[data-theme='dark'] .unihh-bg {
  background:
    radial-gradient(120% 90% at 50% -20%, rgba(59, 91, 219, 0.22) 0%, rgba(59, 91, 219, 0) 60%),
    linear-gradient(180deg, #070b16 0%, #0a1020 55%, #0b0f1c 100%);
}

/* —— 顶部聚焦光 —— */
.unihh-bg__toplight {
  position: absolute;
  top: -22vh;
  left: 50%;
  width: 88vw;
  height: 62vh;
  transform: translateX(-50%);
  background: radial-gradient(
    50% 50% at 50% 50%,
    rgba(96, 140, 255, 0.16) 0%,
    rgba(96, 140, 255, 0) 70%
  );
  filter: blur(48px);
}

html[data-theme='dark'] .unihh-bg__toplight {
  background: radial-gradient(
    50% 50% at 50% 50%,
    rgba(88, 130, 255, 0.2) 0%,
    rgba(88, 130, 255, 0) 70%
  );
}

/* —— 极光渐变 —— */
.unihh-bg__aurora {
  position: absolute;
  inset: 0;
  opacity: var(--unihh-aurora-opacity, 0.26);
}

.unihh-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(110px);
  will-change: transform;
}

.unihh-blob--1 {
  width: 54vw;
  height: 54vw;
  min-width: 480px;
  min-height: 480px;
  top: -16vw;
  left: -12vw;
  background: radial-gradient(circle at 38% 38%, #3d6bff 0%, rgba(61, 107, 255, 0) 66%);
  animation: unihh-drift-1 34s ease-in-out infinite alternate;
}

.unihh-blob--2 {
  width: 44vw;
  height: 44vw;
  min-width: 400px;
  min-height: 400px;
  top: 4vh;
  right: -14vw;
  background: radial-gradient(circle at 60% 40%, #2fd4e8 0%, rgba(47, 212, 232, 0) 64%);
  animation: unihh-drift-2 44s ease-in-out infinite alternate;
}

.unihh-blob--3 {
  width: 52vw;
  height: 52vw;
  min-width: 460px;
  min-height: 460px;
  bottom: -20vw;
  left: 24vw;
  background: radial-gradient(circle at 50% 50%, #7c6cf0 0%, rgba(124, 108, 240, 0) 66%);
  animation: unihh-drift-3 54s ease-in-out infinite alternate;
}

@keyframes unihh-drift-1 {
  from { transform: translate3d(0, 0, 0) scale(1); }
  to { transform: translate3d(8vw, 6vh, 0) scale(1.12); }
}

@keyframes unihh-drift-2 {
  from { transform: translate3d(0, 0, 0) scale(1.04); }
  to { transform: translate3d(-7vw, 10vh, 0) scale(0.94); }
}

@keyframes unihh-drift-3 {
  from { transform: translate3d(0, 0, 0) scale(0.96); }
  to { transform: translate3d(9vw, -8vh, 0) scale(1.1); }
}

/* —— 斜向光束 —— */
.unihh-bg__beam {
  position: absolute;
  top: -30%;
  right: -12%;
  width: 56vw;
  height: 150%;
  transform: rotate(10deg);
  background: linear-gradient(
    100deg,
    transparent 32%,
    rgba(110, 150, 255, 0.09) 47%,
    rgba(186, 150, 255, 0.07) 54%,
    transparent 70%
  );
  filter: blur(32px);
}

/* —— 噪点纹理 —— */
.unihh-bg__grain {
  position: absolute;
  inset: 0;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='160' height='160'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='2' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='160' height='160' filter='url(%23n)' opacity='0.5'/%3E%3C/svg%3E");
  background-size: 160px 160px;
  opacity: 0.32;
  mix-blend-mode: overlay;
}

html[data-theme='dark'] .unihh-bg__grain {
  opacity: 0.22;
  mix-blend-mode: soft-light;
}

/* —— 减弱动效偏好:静止 —— */
@media (prefers-reduced-motion: reduce) {
  .unihh-blob {
    animation: none;
  }
}
</style>
