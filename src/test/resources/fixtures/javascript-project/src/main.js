const hello = require('./index');

const name = process.argv.length > 2 ? process.argv[2] : 'world';

console.log(hello.sayHello(name));
