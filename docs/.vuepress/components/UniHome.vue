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
        <div class="uni-hero__actions">
          <a class="uni-btn uni-btn--primary" :href="t.links.quickstart">
            {{ t.ctaQuickstart }}
            <svg viewBox="0 0 24 24" class="uni-btn__arrow" aria-hidden="true">
              <path d="M5 12h14M13 6l6 6-6 6" />
            </svg>
          </a>
          <a class="uni-btn uni-btn--ghost" :href="repo" target="_blank" rel="noopener">
            <svg viewBox="0 0 24 24" class="uni-btn__icon" aria-hidden="true">
              <path d="M9 19c-4.3 1.4-4.3-2.5-6-3m12 5v-3.5c0-1 .1-1.4-.5-2 2.8-.3 5.5-1.4 5.5-6a4.6 4.6 0 0 0-1.3-3.2 4.2 4.2 0 0 0-.1-3.2s-1.1-.3-3.5 1.3a12.3 12.3 0 0 0-6.2 0C6.5 2.8 5.4 3.1 5.4 3.1a4.2 4.2 0 0 0-.1 3.2A4.6 4.6 0 0 0 4 9.5c0 4.6 2.7 5.7 5.5 6-.6.6-.6 1.2-.5 2V21" />
            </svg>
            GitHub
          </a>
          <a class="uni-btn uni-btn--text" :href="t.links.manual">{{ t.ctaManual }}</a>
        </div>
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
      <div class="uni-features__grid">
        <a
          v-for="f in t.features"
          :key="f.title"
          class="uni-card"
          :href="f.link"
        >
          <span class="uni-card__icon" v-html="f.icon"></span>
          <h3 class="uni-card__title">{{ f.title }}</h3>
          <p class="uni-card__desc">{{ f.desc }}</p>
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
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { withBase } from 'vuepress/client'

const route = useRoute()
const repo = 'https://github.com/unihh-iot/UNIHH'

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
    { icon: iconCpu, title: '多协议采集', desc: 'Modbus、S7、OPC UA 驱动开箱即用，覆盖主流工业设备。', link: withBase('/manual/drivers.html') },
    { icon: iconBlocks, title: '驱动进程隔离', desc: '每个协议独立微服务运行，单驱动故障不扩散、随启随停。', link: withBase('/guide/architecture.html') },
    { icon: iconLock, title: '读写公平互斥', desc: '通道级锁串行采集与反写，RS485 总线不再粘包错帧。', link: withBase('/manual/devices.html') },
    { icon: iconFeather, title: '边缘级轻量', desc: '核心服务 128MB、单驱动 64MB 以内，树莓派全天候常驻。', link: withBase('/guide/deploy.html') },
    { icon: iconBell, title: '规则与告警', desc: '阈值、区间、越界三类判定，异常秒级联动与记录。', link: withBase('/manual/rules.html') },
    { icon: iconDatabase, title: '多引擎存储', desc: 'TDengine 等 5 种存储策略路由，按业务场景灵活落库。', link: withBase('/manual/data.html') },
  ],
  ctaTitle: '三十分钟，跑通第一条采集链路',
  ctaDesc: '从启动容器到看到实时点位数据，只需按文档逐步执行。',
}

const en = {
  badge: 'Open Source · Apache 2.0 · Free for Commercial Use',
  tagline: 'Lightweight IoT platform for the industrial edge',
  sub: 'Device acquisition, reverse control and rule-based alerting — fully open, front to back.',
  shotAlt: 'UNIHH-IOT dashboard',
  ctaQuickstart: 'Get Started',
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
    { icon: iconCpu, title: 'Multi-protocol', desc: 'Modbus, S7 and OPC UA drivers out of the box for mainstream industrial devices.', link: withBase('/en/manual/drivers.html') },
    { icon: iconBlocks, title: 'Process isolation', desc: 'Every driver runs as its own microservice — faults stay contained, restart in seconds.', link: withBase('/en/guide/architecture.html') },
    { icon: iconLock, title: 'Fair bus mutex', desc: 'Channel-level locks serialize reads and writes, ending RS485 framing corruption.', link: withBase('/en/manual/devices.html') },
    { icon: iconFeather, title: 'Edge-ready', desc: '128MB core and under 64MB per driver — runs around the clock on a Raspberry Pi.', link: withBase('/en/guide/deploy.html') },
    { icon: iconBell, title: 'Rules & alerts', desc: 'Threshold, range and boundary checks with second-level actions and logging.', link: withBase('/en/manual/rules.html') },
    { icon: iconDatabase, title: 'Pluggable storage', desc: 'Route writes across five engines including TDengine, per scenario.', link: withBase('/en/manual/data.html') },
  ],
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
  border-color: rgba(30, 111, 255, 0.4);
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
  display: block;
  padding: 26px 24px;
  border: 1px solid var(--uh-card-border);
  border-radius: 16px;
  background: var(--uh-card-bg);
  -webkit-backdrop-filter: blur(12px);
  backdrop-filter: blur(12px);
  text-decoration: none !important;
  transition: transform 0.3s ease, box-shadow 0.3s ease, border-color 0.3s ease;
}

.uni-card:hover {
  transform: translateY(-5px);
  border-color: rgba(30, 111, 255, 0.42);
  box-shadow:
    0 18px 44px var(--uh-glow),
    0 6px 14px rgba(15, 23, 42, 0.08);
}

.uni-card__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 46px;
  height: 46px;
  border-radius: 13px;
  color: var(--uh-c1);
  background:
    linear-gradient(135deg, rgba(30, 111, 255, 0.12), rgba(34, 211, 238, 0.14));
  border: 1px solid var(--uh-card-border);
  transition: background 0.3s ease, color 0.3s ease, transform 0.3s ease;
}

.uni-card__icon :deep(svg) {
  width: 23px;
  height: 23px;
}

.uni-card:hover .uni-card__icon {
  color: #fff;
  background: linear-gradient(135deg, var(--uh-c1), var(--uh-c2));
  transform: scale(1.06);
}

.uni-card__title {
  margin: 16px 0 0;
  font-size: 17px;
  font-weight: 700;
  color: var(--uh-text);
}

.uni-card__desc {
  margin: 9px 0 0;
  font-size: 14px;
  line-height: 1.75;
  color: var(--uh-muted);
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
  .uni-card__icon {
    animation: none !important;
    transition: none !important;
  }
}
</style>
