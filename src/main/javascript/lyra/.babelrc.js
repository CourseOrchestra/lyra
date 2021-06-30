module.exports = {
    presets: [['@babel/env']],
    plugins: [
        'rewire',
        ['istanbul', {extension: ['.js', '.vue']}]
    ]
};
