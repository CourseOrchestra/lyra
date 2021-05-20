module.exports = {
    presets: [['@babel/env']],
    plugins: [
        'rewire',
        ['babel-plugin-istanbul', {
            extension: ['.js', '.vue']
        }]
    ]
};
