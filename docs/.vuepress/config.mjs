import { viteBundler } from '@vuepress/bundler-vite'
import { defaultTheme } from '@vuepress/theme-default'
import { searchPlugin } from '@vuepress/plugin-search'

export default {
  base: '/',
  bundler: viteBundler(),
  title: 'UNIHH-IOT',
  description: '面向工业现场的轻量级开源物联网平台操作文档：设备采集、反向控制、规则告警。',

  head: [
    ['link', { rel: 'icon', href: '/images/logo.svg' }],
    ['meta', { name: 'keywords', content: 'UNIHH-IOT,IoT,IIoT,Modbus,S7,OPC UA,TDengine,SCADA,data acquisition' }],
  ],

  locales: {
    '/': {
      lang: 'zh-CN',
      description: '面向工业现场的轻量级开源物联网平台操作文档：设备采集、反向控制、规则告警。',
    },
    '/en/': {
      lang: 'en-US',
      description: 'Official documentation for UNIHH-IOT, a lightweight open-source IIoT platform for device acquisition, control and alerting.',
    },
  },

  markdown: {
    headers: {
      level: [2, 3, 4],
    },
  },

  plugins: [
    searchPlugin({
      hotKeys: [{ key: 'k', ctrl: true }],
      locales: {
        '/': {
          placeholder: '搜索文档',
        },
        '/en/': {
          placeholder: 'Search docs',
        },
      },
    }),
  ],

  theme: defaultTheme({
    logo: '/images/logo.svg',
    repo: 'unihh-iot/UNIHH',
    docsDir: 'docs',
    docsBranch: 'main',
    editLink: true,

    locales: {
      '/': {
        selectLanguageName: '简体中文',
        selectText: '语言',
        tip: '提示',
        warning: '注意',
        danger: '警告',
        editLinkText: '在 GitHub 上编辑此页',
        lastUpdatedText: '上次更新',
        outline: [2, 3],
        sidebarDepth: 2,
        navbar: [
          { text: '首页', link: '/' },
          { text: '使用指南', link: '/guide/' },
          { text: '操作手册', link: '/manual/' },
        ],
        sidebar: {
          '/guide/': [
            {
              text: '使用指南',
              collapsible: false,
              children: [
                '/guide/',
                '/guide/quickstart.md',
                '/guide/architecture.md',
                '/guide/driver-dev.md',
                '/guide/deploy.md',
              ],
            },
          ],
          '/manual/': [
            {
              text: '操作手册',
              collapsible: false,
              children: [
                '/manual/',
                '/manual/dashboard.md',
                '/manual/drivers.md',
                '/manual/devices.md',
                '/manual/thingmodels.md',
                '/manual/data.md',
                '/manual/rules.md',
                '/manual/alarms.md',
                '/manual/logs.md',
                '/manual/system.md',
              ],
            },
          ],
          '/reference/': [
            {
              text: '参考',
              collapsible: false,
              children: [
                '/reference/api.md',
                '/reference/mqtt-topics.md',
                '/reference/faq.md',
              ],
            },
          ],
        },
      },
      '/en/': {
        selectLanguageName: 'English',
        selectText: 'Languages',
        tip: 'Tip',
        warning: 'Note',
        danger: 'Warning',
        editLinkText: 'Edit this page on GitHub',
        lastUpdatedText: 'Last Updated',
        outline: [2, 3],
        sidebarDepth: 2,
        navbar: [
          { text: 'Home', link: '/en/' },
          { text: 'Guide', link: '/en/guide/' },
          { text: 'Manual', link: '/en/manual/' },
        ],
        sidebar: {
          '/en/guide/': [
            {
              text: 'Guide',
              collapsible: false,
              children: [
                '/en/guide/',
                '/en/guide/quickstart.md',
                '/en/guide/architecture.md',
                '/en/guide/driver-dev.md',
                '/en/guide/deploy.md',
              ],
            },
          ],
          '/en/manual/': [
            {
              text: 'Manual',
              collapsible: false,
              children: [
                '/en/manual/',
                '/en/manual/dashboard.md',
                '/en/manual/drivers.md',
                '/en/manual/devices.md',
                '/en/manual/thingmodels.md',
                '/en/manual/data.md',
                '/en/manual/rules.md',
                '/en/manual/alarms.md',
                '/en/manual/logs.md',
                '/en/manual/system.md',
              ],
            },
          ],
          '/en/reference/': [
            {
              text: 'Reference',
              collapsible: false,
              children: [
                '/en/reference/api.md',
                '/en/reference/mqtt-topics.md',
                '/en/reference/faq.md',
              ],
            },
          ],
        },
      },
    },
  }),
}
