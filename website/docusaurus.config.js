module.exports = {
  title: 'Alejandro Hernández',
  url: 'https://alejandrohdezma.com',
  baseUrl: '/',
  onBrokenLinks: 'throw',
  onBrokenMarkdownLinks: 'warn',
  favicon: 'img/favicon.ico',
  organizationName: 'alejandrohdezma',
  projectName: 'alejandrohdezma',
  themeConfig: {
    navbar: {
      title: 'Welcome to my blog!'
    },
    colorMode: {
      respectPrefersColorScheme: true
    },
    footer: {
      style: 'dark',
      links: [
        {
          title: 'Available at',
          items: [
            {
              label: 'Github',
              href: 'https://github.com/alejandrohdezma',
            },
            {
              label: 'Twitter',
              href: 'https://twitter.com/alejandrohdezma',
            },
          ],
        },
        {
          title: 'Some of my Open Source projects',
          items: [
            {
              label: 'sbt-github',
              href: 'https://github.com/alejandrohdezma/sbt-github',
            },
            {
              label: 'scala-steward-action',
              to: 'https://github.com/scala-steward-org/scala-steward-action',
            },
          ],
        },
        {
          title: '‏‏‎ ',
          items: [
            {
              label: 'sbt-modules',
              href: 'https://github.com/alejandrohdezma/sbt-modules',
            },
            {
              label: 'sbt-fix',
              to: 'https://github.com/alejandrohdezma/sbt-fix',
            },
          ],
        },
      ],
      copyright: `Copyright © ${new Date().getFullYear()} Alejandro Hernández.`,
    },
  },
  presets: [
    [
      '@docusaurus/preset-classic',
      {
        docs: false,
        pages: false,
        blog: {
          feedOptions: {
            type: 'all',
            copyright: `Copyright © ${new Date().getFullYear()} Alejandro Hernández`,
          },
          blogTitle: 'miau',
          showReadingTime: true,
          path: './blog',
          routeBasePath: '/',
          editUrl: 'https://github.com/alejandrohdezma/alejandrohdezma/edit/master/website/blog/',
        },
        theme: {
          customCss: require.resolve('./src/css/custom.css'),
        },
      },
    ],
  ],
};
