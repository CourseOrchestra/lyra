module.exports = function getConfig(env) {
  const dojoConfig = {
    baseUrl: '.',
    packages: [
      {
        name: 'dojo',
        location: `${env.dojoRoot}/dojo`,
        lib: '.',
      },
    ],
    async: true,
    has: { 'dojo-config-api': 0 },
  };
  return dojoConfig;
};
