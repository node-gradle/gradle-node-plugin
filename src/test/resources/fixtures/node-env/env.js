const fail = process.argv.indexOf('fail') !== -1;
if (fail) {
    throw new Error('I had to fail');
}

const customEnvironmentVariable = process.env.CUSTOM;
if (customEnvironmentVariable) {
    console.log(`Detected custom environment: ${customEnvironmentVariable}`);
} else {
    console.log('No custom environment');
}
