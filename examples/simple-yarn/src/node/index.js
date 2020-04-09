const figlet = require('figlet');

const out = figlet.textSync('Hello World!', {
    font: 'Standard'
});

console.log(out);
