const hello = require('../src');
const sayHello = hello.sayHello;
const chai = require('chai');
const expect = chai.expect;

describe('hello', () => {
    describe('#sayHello()', () => {
        it('should say hello', () => {
            expect(sayHello('World')).to.equal('Hello World!');
        });
    });
});
