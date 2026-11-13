<template>
  <div class="uni-home">
    <!-- ===== Hero ===== -->
    <section class="uni-hero">
      <div class="uni-hero__inner">
        <p class="uni-hero__badge">
          <span class="uni-hero__badge-dot"></span>
          {{ t.badge }}
        </p>
        <h1 class="uni-hero__title">UNIHH-IOT</h1>
        <p class="uni-hero__tagline">{{ t.tagline }}</p>
        <p class="uni-hero__sub">{{ t.sub }}</p>
        <div class="uni-hero__shields">
          <span class="uni-shield">
            <img src="https://img.shields.io/badge/license-Apache%202.0-green?style=flat-square" alt="Apache 2.0 License" loading="lazy" />
          </span>
          <span class="uni-shield">
            <img src="https://img.shields.io/badge/JDK-17%2B-orange?style=flat-square&logo=openjdk&logoColor=white" alt="JDK 17+" loading="lazy" />
          </span>
          <span class="uni-shield">
            <img src="https://img.shields.io/badge/Spring%20Boot-2.7.18-brightgreen?style=flat-square&logo=springboot&logoColor=white" alt="Spring Boot 2.7.18" loading="lazy" />
          </span>
          <span class="uni-shield">
            <img src="https://img.shields.io/badge/EMQX-5.x-009A61?style=flat-square&logo=mqtt&logoColor=white" alt="EMQX 5.x" loading="lazy" />
          </span>
          <span class="uni-shield">
            <img src="https://img.shields.io/badge/Docker-Compose%20%E4%B8%80%E9%94%AE%E9%83%A8%E7%BD%B2-2496ED?style=flat-square&logo=docker&logoColor=white" alt="Docker Compose" loading="lazy" />
          </span>
          <span class="uni-shield">
            <img src="https://img.shields.io/github/stars/unihh-iot/UNIHH?style=flat-square&logo=github" alt="GitHub stars" loading="lazy" />
          </span>
          <span class="uni-shield">
            <img src="https://gitee.com/giteeRepo/unihh/badge/star.svg?theme=dark" alt="Gitee stars" loading="lazy" />
          </span>
          <span class="uni-shield">
            <img src="https://img.shields.io/badge/%E5%9C%A8%E7%BA%BF%E6%BC%94%E7%A4%BA-demo.unihh.com-1e6fff?style=flat-square" alt="Online demo" loading="lazy" />
          </span>
        </div>
        <div class="uni-hero__actions">
          <a class="uni-btn uni-btn--primary" :href="t.links.quickstart">
            {{ t.ctaQuickstart }}
            <svg viewBox="0 0 24 24" class="uni-btn__arrow" aria-hidden="true">
              <path d="M5 12h14M13 6l6 6-6 6" />
            </svg>
          </a>
          <a class="uni-btn uni-btn--ghost" :href="repo" target="_blank" rel="noopener">
            GitHub
          </a>
          <a class="uni-btn uni-btn--text" :href="t.links.manual">{{ t.ctaManual }}</a>
        </div>
        <a class="uni-hero__demo" :href="demoUrl" target="_blank" rel="noopener">
          <span class="uni-demo__live">
            <span class="uni-demo__dot"></span>
            {{ t.ctaDemo }}
          </span>
          <span class="uni-demo__url">demo.unihh.com</span>
          <span class="uni-demo__divider"></span>
          <span class="uni-demo__cred">
            {{ t.demoHint }}
            <code>admin</code>
            <span class="uni-demo__sep">/</span>
            <code>unihhiot</code>
          </span>
        </a>
        <div class="uni-hero__shot">
          <div class="uni-hero__shot-bar">
            <span></span><span></span><span></span>
          </div>
          <img src="/images/dashboard.png" :alt="t.shotAlt" loading="eager" />
        </div>
      </div>
    </section>

    <!-- ===== Stats ===== -->
    <section class="uni-stats">
      <div class="uni-stats__inner">
        <div v-for="s in t.stats" :key="s.label" class="uni-stats__item">
          <p class="uni-stats__value">{{ s.value }}</p>
          <p class="uni-stats__label">{{ s.label }}</p>
        </div>
      </div>
    </section>

    <!-- ===== Features ===== -->
    <section class="uni-features">
      <p class="uni-sec__overline">{{ t.featuresOverline }}</p>
      <h2 class="uni-sec__title">{{ t.featuresTitle }}</h2>
      <p class="uni-sec__desc">{{ t.featuresDesc }}</p>
      <div class="uni-features__grid" :class="{ 'uni-features__grid--anim': featuresAnim }">
        <a
          v-for="(f, i) in t.features"
          :key="f.title"
          class="uni-card"
          :class="{ 'is-visible': visibleAll }"
          :style="{ '--acc': f.accent, '--d': i }"
          :href="f.link"
        >
          <span class="uni-card__num">0{{ i + 1 }}</span>
          <span class="uni-card__icon" v-html="f.icon"></span>
          <h3 class="uni-card__title">{{ f.title }}</h3>
          <p class="uni-card__desc">{{ f.desc }}</p>
          <span class="uni-card__more">
            {{ t.more }}
            <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M5 12h14M13 6l6 6-6 6" /></svg>
          </span>
        </a>
      </div>
    </section>

    <!-- ===== CTA ===== -->
    <section class="uni-cta">
      <div class="uni-cta__panel">
        <h2 class="uni-cta__title">{{ t.ctaTitle }}</h2>
        <p class="uni-cta__desc">{{ t.ctaDesc }}</p>
        <a class="uni-btn uni-btn--primary uni-btn--lg" :href="t.links.quickstart">
          {{ t.ctaStart }}
          <svg viewBox="0 0 24 24" class="uni-btn__arrow" aria-hidden="true">
            <path d="M5 12h14M13 6l6 6-6 6" />
          </svg>
        </a>
      </div>
    </section>

    <!-- ===== Footer ===== -->
    <footer class="uni-footer">
      <p>Apache-2.0 Licensed · Copyright © 2026
        <a :href="repo" target="_blank" rel="noopener">unihh.com</a>
      </p>
    </footer>
  </div>
</template>

<script setup>
import { computed, onMounted, onBeforeUnmount, ref } from 'vue'
import { useRoute } from 'vue-router'
import { withBase } from 'vuepress/client'

const route = useRoute()
const repo = 'https://github.com/unihh-iot/UNIHH'
const demoUrl = 'https://demo.unihh.com'

/* 滚动进入视口后逐卡交错上浮 */
const featuresAnim = ref(false)
const visibleAll = ref(false)
let io = null

onMounted(() => {
  const grid = document.querySelector('.uni-features__grid')
  if (!grid || !('IntersectionObserver' in window)) return
  featuresAnim.value = true
  io = new IntersectionObserver(
    (entries) => {
      if (entries.some((e) => e.isIntersecting)) {
        visibleAll.value = true
        io.disconnect()
        io = null
      }
    },
    { threshold: 0.15 },
  )
  io.observe(grid)
})

onBeforeUnmount(() => {
  if (io) io.disconnect()
})

const iconCpu = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><rect x="5" y="5" width="14" height="14" rx="2"/><rect x="10" y="10" width="4" height="4"/><path d="M9 2v3M15 2v3M9 19v3M15 19v3M2 9h3M2 15h3M19 9h3M19 15h3"/></svg>'
const iconBlocks = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="7" height="7" rx="1.5"/><rect x="14" y="3" width="7" height="7" rx="1.5"/><rect x="3" y="14" width="7" height="7" rx="1.5"/><path d="M17.5 14v7M14 17.5h7"/></svg>'
const iconLock = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><rect x="4" y="11" width="16" height="10" rx="2"/><path d="M8 11V7a4 4 0 0 1 8 0v4"/></svg>'
const iconFeather = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path d="M20.24 12.24a6 6 0 0 0-8.49-8.49L5 10.5V19h8.5z"/><path d="M16 8 2 22"/><path d="M17.5 15H9"/></svg>'
const iconBell = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9"/><path d="M10.3 21a1.94 1.94 0 0 0 3.4 0"/></svg>'
const iconDatabase = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><ellipse cx="12" cy="5" rx="9" ry="3"/><path d="M3 5v14a9 3 0 0 0 18 0V5"/><path d="M3 12a9 3 0 0 0 18 0"/></svg>'

const zh = {
  badge: '开源 · Apache 2.0 · 免费商用',
  tagline: '面向工业现场的轻量级物联网平台',
  sub: '设备采集、反向控制、规则告警，前后端代码全部开放。',
  shotAlt: 'UNIHH-IOT 监控大盘',
  ctaQuickstart: '快速开始',
  ctaDemo: '在线演示',
  demoHint: '演示账号：',
  ctaManual: '操作手册 →',
  ctaStart: '立即上手',
  links: {
    quickstart: withBase('/guide/quickstart.html'),
    manual: withBase('/manual/'),
  },
  stats: [
    { value: '3 类', label: '开箱协议驱动' },
    { value: '128MB', label: '核心堆内存上限' },
    { value: '5 种', label: '时序存储引擎' },
    { value: '秒级', label: '告警联动触达' },
  ],
  featuresOverline: 'CORE CAPABILITIES',
  featuresTitle: '为工业现场而生',
  featuresDesc: '从总线互斥到进程隔离，每个设计都来自真实的产线与泵站场景。',
  features: [
    { icon: iconCpu, title: '多协议采集', desc: 'Modbus、S7、OPC UA 驱动开箱即用，覆盖主流工业设备。', link: withBase('/manual/drivers.html'), accent: '#1e6fff' },
    { icon: iconBlocks, title: '驱动进程隔离', desc: '每个协议独立微服务运行，单驱动故障不扩散、随启随停。', link: withBase('/guide/architecture.html'), accent: '#06b6d4' },
    { icon: iconLock, title: '读写公平互斥', desc: '通道级锁串行采集与反写，RS485 总线不再粘包错帧。', link: withBase('/manual/devices.html'), accent: '#8b5cf6' },
    { icon: iconFeather, title: '边缘级轻量', desc: '核心服务 128MB、单驱动 64MB 以内，树莓派全天候常驻。', link: withBase('/guide/deploy.html'), accent: '#10b981' },
    { icon: iconBell, title: '规则与告警', desc: '阈值、区间、越界三类判定，异常秒级联动与记录。', link: withBase('/manual/rules.html'), accent: '#f59e0b' },
    { icon: iconDatabase, title: '多引擎存储', desc: 'TDengine 等 5 种存储策略路由，按业务场景灵活落库。', link: withBase('/manual/data.html'), accent: '#f43f5e' },
  ],
  more: '了解更多',
  ctaTitle: '三十分钟，跑通第一条采集链路',
  ctaDesc: '从启动容器到看到实时点位数据，只需按文档逐步执行。',
}

const en = {
  badge: 'Open Source · Apache 2.0 · Free for Commercial Use',
  tagline: 'Lightweight IoT platform for the industrial edge',
  sub: 'Device acquisition, reverse control and rule-based alerting — fully open, front to back.',
  shotAlt: 'UNIHH-IOT dashboard',
  ctaQuickstart: 'Get Started',
  ctaDemo: 'Live Demo',
  demoHint: 'Demo account:',
  ctaManual: 'User Manual →',
  ctaStart: 'Start Now',
  links: {
    quickstart: withBase('/en/guide/quickstart.html'),
    manual: withBase('/en/manual/'),
  },
  stats: [
    { value: '3', label: 'protocol drivers built-in' },
    { value: '128MB', label: 'core heap ceiling' },
    { value: '5', label: 'storage engines' },
    { value: 'Seconds', label: 'alert delivery' },
  ],
  featuresOverline: 'CORE CAPABILITIES',
  featuresTitle: 'Built for the industrial field',
  featuresDesc: 'From bus-level mutex to process isolation, every design choice comes from real production lines and pump stations.',
  features: [
    { icon: iconCpu, title: 'Multi-protocol', desc: 'Modbus, S7 and OPC UA drivers out of the box for mainstream industrial devices.', link: withBase('/en/manual/drivers.html'), accent: '#1e6fff' },
    { icon: iconBlocks, title: 'Process isolation', desc: 'Every driver runs as its own microservice — faults stay contained, restart in seconds.', link: withBase('/en/guide/architecture.html'), accent: '#06b6d4' },
    { icon: iconLock, title: 'Fair bus mutex', desc: 'Channel-level locks serialize reads and writes, ending RS485 framing corruption.', link: withBase('/en/manual/devices.html'), accent: '#8b5cf6' },
    { icon: iconFeather, title: 'Edge-ready', desc: '128MB core and under 64MB per driver — runs around the clock on a Raspberry Pi.', link: withBase('/en/guide/deploy.html'), accent: '#10b981' },
    { icon: iconBell, title: 'Rules & alerts', desc: 'Threshold, range and boundary checks with second-level actions and logging.', link: withBase('/en/manual/rules.html'), accent: '#f59e0b' },
    { icon: iconDatabase, title: 'Pluggable storage', desc: 'Route writes across five engines including TDengine, per scenario.', link: withBase('/en/manual/data.html'), accent: '#f43f5e' },
  ],
  more: 'Learn more',
  ctaTitle: 'Your first acquisition pipeline in 30 minutes',
  ctaDesc: 'From starting the containers to watching live data points — just follow the docs.',
}

const t = computed(() => (route.path.startsWith('/en') ? en : zh))
</script>

<style scoped>
.uni-home {
  --uh-c1: #1e6fff;
  --uh-c2: #22d3ee;
  --uh-c3: #8b5cf6;
  --uh-text: #1b2a4a;
  --uh-muted: #5a6b8c;
  --uh-card-bg: rgba(255, 255, 255, 0.72);
  --uh-card-border: rgba(30, 111, 255, 0.14);
  --uh-glow: rgba(30, 111, 255, 0.2);
}

html[data-theme='dark'] .uni-home {
  --uh-text: #e5edff;
  --uh-muted: #93a5c8;
  --uh-card-bg: rgba(13, 20, 38, 0.62);
  --uh-card-border: rgba(125, 211, 252, 0.16);
  --uh-glow: rgba(34, 211, 238, 0.22);
}

.uni-home {
  color: var(--uh-text);
  overflow-x: clip;
}

.uni-hero,
.uni-stats,
.uni-features,
.uni-cta {
  width: 100%;
  box-sizing: border-box;
}

.uni-hero__inner,
.uni-stats__inner,
.uni-features,
.uni-cta__panel {
  max-width: 1152px;
  margin: 0 auto;
  padding: 0 24px;
}

/* ===== Hero ===== */
.uni-hero {
  padding: clamp(56px, 9vh, 104px) 0 0;
  text-align: center;
}

.uni-hero__badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 16px;
  border-radius: 999px;
  border: 1px solid var(--uh-card-border);
  background: var(--uh-card-bg);
  -webkit-backdrop-filter: blur(10px);
  backdrop-filter: blur(10px);
  font-size: 13px;
  color: var(--uh-muted);
  letter-spacing: 0.04em;
}

.uni-hero__badge-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--uh-c1), var(--uh-c2));
  box-shadow: 0 0 10px var(--uh-glow);
}

/* ===== Shields badges (display only, no links) ===== */
.uni-hero__shields {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  align-items: center;
  gap: 8px;
  margin-top: 20px;
}

.uni-shield {
  display: inline-flex;
  line-height: 0;
}

.uni-shield img {
  height: 20px;
  width: auto;
}

.uni-hero__title {
  margin: 22px 0 0;
  font-size: clamp(44px, 7vw, 76px);
  font-weight: 800;
  line-height: 1.1;
  letter-spacing: 0.01em;
  background: linear-gradient(100deg, var(--uh-c1) 0%, var(--uh-c2) 35%, var(--uh-c3) 70%, var(--uh-c1) 100%);
  background-size: 250% 100%;
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  animation: uni-shimmer 9s linear infinite;
}

.uni-hero__tagline {
  margin: 18px 0 0;
  font-size: clamp(19px, 2.4vw, 25px);
  font-weight: 600;
}

.uni-hero__sub {
  margin: 12px auto 0;
  max-width: 560px;
  font-size: 15px;
  line-height: 1.7;
  color: var(--uh-muted);
}

.uni-hero__actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  align-items: center;
  gap: 14px;
  margin-top: 32px;
}

/* ===== Buttons ===== */
.uni-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 11px 24px;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 600;
  text-decoration: none !important;
  transition: transform 0.25s ease, box-shadow 0.25s ease, border-color 0.25s ease;
}

.uni-btn--primary {
  color: #fff !important;
  background: linear-gradient(120deg, var(--uh-c1), #2f8bff 55%, var(--uh-c2));
  box-shadow: 0 10px 28px var(--uh-glow);
}

.uni-btn--primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 16px 38px var(--uh-glow);
}

.uni-btn--ghost {
  color: var(--uh-text) !important;
  border: 1px solid var(--uh-card-border);
  background: var(--uh-card-bg);
  -webkit-backdrop-filter: blur(10px);
  backdrop-filter: blur(10px);
}

.uni-btn--ghost:hover {
  transform: translateY(-2px);
  color: var(--uh-c1) !important;
  border-color: color-mix(in srgb, var(--uh-c1) 45%, transparent);
}

.uni-btn--text {
  padding: 11px 6px;
  color: var(--uh-muted) !important;
}

.uni-btn--text:hover {
  color: var(--uh-c1) !important;
}

.uni-btn__arrow,
.uni-btn__icon {
  width: 17px;
  height: 17px;
  fill: none;
  stroke: currentColor;
  stroke-width: 2;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.uni-btn--lg {
  padding: 13px 30px;
  font-size: 16px;
}

/* ===== 在线演示条 ===== */
/* 禁用任何来源(主题/缓存)注入的伪元素箭头 */
.uni-hero__demo::before,
.uni-hero__demo::after {
  content: none !important;
  display: none !important;
}

.uni-hero__demo {
  display: inline-flex;
  flex-wrap: wrap;
  justify-content: center;
  align-items: center;
  gap: 8px 14px;
  margin: 28px auto 0;
  padding: 11px 22px;
  border-radius: 999px;
  border: 1px solid var(--uh-card-border);
  background: var(--uh-card-bg);
  -webkit-backdrop-filter: blur(10px);
  backdrop-filter: blur(10px);
  font-size: 13.5px;
  color: var(--uh-text);
  text-decoration: none !important;
  transition: transform 0.3s ease, border-color 0.3s ease, box-shadow 0.3s ease;
}

.uni-hero__demo:hover {
  transform: translateY(-2px);
  border-color: color-mix(in srgb, var(--uh-c1) 45%, transparent);
  box-shadow: 0 12px 32px var(--uh-glow);
}

.uni-demo__live {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  font-weight: 700;
}

.uni-demo__dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #10b981;
  animation: uni-pulse 2.2s ease-out infinite;
}

@keyframes uni-pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(16, 185, 129, 0.45);
  }
  70% {
    box-shadow: 0 0 0 7px rgba(16, 185, 129, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(16, 185, 129, 0);
  }
}

.uni-demo__url {
  font-family: ui-monospace, 'Cascadia Code', Consolas, monospace;
  font-weight: 600;
  color: var(--uh-c1);
}

.uni-demo__divider {
  width: 1px;
  height: 16px;
  background: var(--uh-card-border);
}

.uni-demo__cred {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12.5px;
  color: var(--uh-muted);
}

.uni-demo__cred code {
  padding: 2px 8px;
  border-radius: 6px;
  border: 1px solid var(--uh-card-border);
  background: var(--uh-card-bg);
  font-family: ui-monospace, 'Cascadia Code', Consolas, monospace;
  font-size: 11.5px;
  line-height: 1.4;
  color: var(--uh-c1);
}

.uni-demo__sep {
  color: var(--uh-muted);
}

@media (max-width: 560px) {
  .uni-demo__divider {
    display: none;
  }
}

/* ===== Hero screenshot ===== */
.uni-hero__shot {
  max-width: 920px;
  margin: 52px auto 0;
  border-radius: 16px;
  border: 1px solid var(--uh-card-border);
  background: var(--uh-card-bg);
  -webkit-backdrop-filter: blur(14px);
  backdrop-filter: blur(14px);
  box-shadow:
    0 32px 80px var(--uh-glow),
    0 8px 24px rgba(15, 23, 42, 0.1);
  overflow: hidden;
  animation: uni-float 8s ease-in-out infinite;
}

.uni-hero__shot-bar {
  display: flex;
  gap: 7px;
  padding: 12px 16px;
  border-bottom: 1px solid var(--uh-card-border);
}

.uni-hero__shot-bar span {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--uh-card-border);
}

.uni-hero__shot-bar span:first-child {
  background: linear-gradient(135deg, var(--uh-c1), var(--uh-c2));
}

.uni-hero__shot img {
  display: block;
  width: 100%;
  height: auto;
}

/* ===== Stats ===== */
.uni-stats {
  padding: 0 24px;
  margin-top: -1px;
}

.uni-stats__inner {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  padding: 34px 28px;
  border: 1px solid var(--uh-card-border);
  border-radius: 18px;
  background: var(--uh-card-bg);
  -webkit-backdrop-filter: blur(14px);
  backdrop-filter: blur(14px);
  box-shadow: 0 18px 48px rgba(15, 23, 42, 0.08);
  transform: translateY(-46px);
}

.uni-stats__item {
  text-align: center;
}

.uni-stats__item + .uni-stats__item {
  border-left: 1px solid var(--uh-card-border);
}

.uni-stats__value {
  margin: 0;
  font-size: clamp(24px, 3vw, 34px);
  font-weight: 800;
  background: linear-gradient(120deg, var(--uh-c1), var(--uh-c2));
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.uni-stats__label {
  margin: 6px 0 0;
  font-size: 13px;
  color: var(--uh-muted);
}

/* ===== Section headers ===== */
.uni-sec__overline {
  margin: 0;
  text-align: center;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.32em;
  color: var(--uh-c1);
  text-transform: uppercase;
}

.uni-sec__title {
  margin: 14px 0 0;
  text-align: center;
  font-size: clamp(26px, 3.4vw, 36px);
  font-weight: 800;
  border: none;
  padding: 0;
}

.uni-sec__desc {
  margin: 12px auto 0;
  max-width: 520px;
  text-align: center;
  font-size: 15px;
  line-height: 1.7;
  color: var(--uh-muted);
}

/* ===== Features ===== */
.uni-features {
  padding-top: 40px;
}

.uni-features__grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 18px;
  margin-top: 40px;
}

.uni-card {
  position: relative;
  display: block;
  padding: 28px 26px;
  border: 1px solid var(--uh-card-border);
  border-radius: 16px;
  background: var(--uh-card-bg);
  -webkit-backdrop-filter: blur(12px);
  backdrop-filter: blur(12px);
  text-decoration: none !important;
  overflow: hidden;
  transition: transform 0.35s cubic-bezier(0.22, 1, 0.36, 1), box-shadow 0.35s ease, border-color 0.35s ease;
}

/* 顶部渐变细线：hover 自中心展开 */
.uni-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 14%;
  width: 72%;
  height: 2px;
  border-radius: 999px;
  background: linear-gradient(90deg, transparent, var(--acc), transparent);
  opacity: 0;
  transform: scaleX(0.3);
  transition: opacity 0.4s ease, transform 0.45s cubic-bezier(0.22, 1, 0.36, 1);
}

/* 右上角品牌色光斑 */
.uni-card::after {
  content: '';
  position: absolute;
  top: -36px;
  right: -36px;
  width: 150px;
  height: 150px;
  border-radius: 50%;
  background: radial-gradient(circle, var(--acc) 0%, transparent 68%);
  opacity: 0.1;
  transition: opacity 0.45s ease;
  pointer-events: none;
}

.uni-card:hover {
  transform: translateY(-6px);
  border-color: color-mix(in srgb, var(--acc) 42%, transparent);
  box-shadow:
    0 22px 48px -12px color-mix(in srgb, var(--acc) 30%, transparent),
    0 8px 20px rgba(15, 23, 42, 0.08);
}

.uni-card:hover::before {
  opacity: 1;
  transform: scaleX(1);
}

.uni-card:hover::after {
  opacity: 0.32;
}

/* 右上角 mono 序号 */
.uni-card__num {
  position: absolute;
  top: 16px;
  right: 20px;
  font-family: ui-monospace, 'Cascadia Code', Consolas, monospace;
  font-size: 34px;
  font-weight: 700;
  line-height: 1;
  color: var(--acc);
  opacity: 0.12;
  transition: opacity 0.4s ease;
  pointer-events: none;
}

.uni-card:hover .uni-card__num {
  opacity: 0.28;
}

.uni-card__icon {
  position: relative;
  z-index: 1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 52px;
  height: 52px;
  border-radius: 14px;
  color: var(--acc);
  background: linear-gradient(135deg, color-mix(in srgb, var(--acc) 12%, transparent), color-mix(in srgb, var(--acc) 22%, transparent));
  border: 1px solid color-mix(in srgb, var(--acc) 22%, transparent);
  transition: background 0.35s ease, color 0.35s ease, transform 0.35s ease, box-shadow 0.35s ease;
}

.uni-card__icon :deep(svg) {
  width: 24px;
  height: 24px;
}

.uni-card:hover .uni-card__icon {
  color: #fff;
  background: linear-gradient(135deg, var(--acc), color-mix(in srgb, var(--acc) 62%, #000));
  transform: scale(1.06);
  box-shadow: 0 8px 22px -6px color-mix(in srgb, var(--acc) 55%, transparent);
}

.uni-card__title {
  position: relative;
  z-index: 1;
  margin: 18px 0 0;
  font-size: 17px;
  font-weight: 700;
  color: var(--uh-text);
}

.uni-card__desc {
  position: relative;
  z-index: 1;
  margin: 9px 0 0;
  font-size: 14px;
  line-height: 1.75;
  color: var(--uh-muted);
}

/* hover 浮现的引导链接（占位不跳动布局） */
.uni-card__more {
  position: relative;
  z-index: 1;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-top: 16px;
  font-size: 13px;
  font-weight: 600;
  color: var(--acc);
  opacity: 0;
  transform: translateY(4px);
  transition: opacity 0.35s ease, transform 0.35s ease;
}

.uni-card__more svg {
  width: 14px;
  height: 14px;
  fill: none;
  stroke: currentColor;
  stroke-width: 2;
  stroke-linecap: round;
  stroke-linejoin: round;
  transition: transform 0.35s ease;
}

.uni-card:hover .uni-card__more {
  opacity: 1;
  transform: none;
}

.uni-card:hover .uni-card__more svg {
  transform: translateX(3px);
}

/* 滚动进入：逐卡交错上浮（animation both 在 delay 期间保持初始态） */
.uni-features__grid--anim .uni-card.is-visible {
  animation: uni-card-rise 0.65s cubic-bezier(0.22, 1, 0.36, 1) both;
  animation-delay: calc(var(--d) * 90ms);
}

@keyframes uni-card-rise {
  from {
    opacity: 0;
    transform: translateY(26px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: none;
  }
}

/* ===== CTA ===== */
.uni-cta {
  padding: 72px 24px 8px;
}

.uni-cta__panel {
  position: relative;
  text-align: center;
  padding: 52px 32px;
  border-radius: 20px;
  border: 1px solid transparent;
  background:
    linear-gradient(var(--uh-card-bg), var(--uh-card-bg)) padding-box,
    linear-gradient(120deg, rgba(30, 111, 255, 0.55), rgba(34, 211, 238, 0.5), rgba(139, 92, 246, 0.5)) border-box;
  -webkit-backdrop-filter: blur(14px);
  backdrop-filter: blur(14px);
  box-shadow: 0 24px 64px var(--uh-glow);
}

.uni-cta__title {
  margin: 0;
  font-size: clamp(22px, 3vw, 30px);
  font-weight: 800;
}

.uni-cta__desc {
  margin: 12px 0 0;
  font-size: 15px;
  color: var(--uh-muted);
}

.uni-cta__panel .uni-btn {
  margin-top: 26px;
}

/* ===== Footer ===== */
.uni-footer {
  margin-top: 64px;
  padding: 26px 24px 34px;
  border-top: 1px solid var(--uh-card-border);
  text-align: center;
  font-size: 13px;
  color: var(--uh-muted);
}

.uni-footer a {
  color: var(--uh-muted);
}

.uni-footer a:hover {
  color: var(--uh-c1);
}

/* ===== Keyframes ===== */
@keyframes uni-shimmer {
  from { background-position: 0% 50%; }
  to { background-position: 250% 50%; }
}

@keyframes uni-float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

/* ===== Responsive ===== */
@media (max-width: 900px) {
  .uni-features__grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .uni-stats__inner {
    grid-template-columns: repeat(2, 1fr);
    gap: 24px 16px;
    transform: translateY(-32px);
  }

  .uni-stats__item:nth-child(3) {
    border-left: none;
  }
}

@media (max-width: 560px) {
  .uni-features__grid {
    grid-template-columns: 1fr;
  }

  .uni-stats__inner {
    grid-template-columns: 1fr 1fr;
  }

  .uni-stats__item + .uni-stats__item {
    border-left: none;
  }
}

/* ===== Reduced motion ===== */
@media (prefers-reduced-motion: reduce) {
  .uni-hero__title,
  .uni-hero__shot,
  .uni-btn,
  .uni-card,
  .uni-card__icon,
  .uni-demo__dot {
    animation: none !important;
    transition: none !important;
  }
}
</style>
