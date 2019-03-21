function getConfig(env) {
    dojoConfig = {
        baseUrl: '.',
        packages: [
            {
                name: 'dojo',
                location: env.dojoRoot + '/dojo',
                lib: '.'
            },
        ],
        async: true,
        has: {'dojo-config-api': 0},
        map: {"*": {"dstore": "dojo-dstore"}},
    };
    return dojoConfig;
}

module.exports = getConfig;
