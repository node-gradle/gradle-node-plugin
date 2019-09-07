const chai = require('chai');
const expect = chai.expect;

describe('Array', () => {
    describe('#indexOf()', () => {
        it('should return -1 when the value is not present', () => {
            expect([1, 2, 3].indexOf(4)).to.equal(-1);
        });
    });
});
