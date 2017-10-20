const solc = require('solc')
const Web3 = require('web3');
const fs = require('fs-extra');

const web3 = new Web3();
web3.setProvider(new web3.providers.HttpProvider('http://localhost:8545'));
const owner = web3.eth.accounts[0];
const code = fs.readFileSync('src/main/resources/Pong.sol').toString();
const compiledCode = solc.compile(code);
const iface = compiledCode.contracts[':Pong'].interface;
const abiDefinition = JSON.parse(iface);
const byteCode = compiledCode.contracts[':Pong'].bytecode;
const PongContract = web3.eth.contract(abiDefinition);

function interface() {
  return iface;
}

function deploy(width, height, paddleLength, paddleWidth, ballSize, minBallSpeed, maxBallSpeed) {
  PongContract.new(
    width, height, paddleLength, paddleWidth, ballSize, minBallSpeed, maxBallSpeed, 0,
    { data: byteCode, from: owner, gas: 4700000 },
    function(error, left) {
      console.log("Left deployed to " + left.address);
    }
  );
  PongContract.new(
    width, height, paddleLength, paddleWidth, ballSize, minBallSpeed, maxBallSpeed, 1,
    { data: byteCode, from: owner, gas: 4700000 },
    function(error, right) {
      console.log("Right deployed to " + right.address);
    }
  );
}

module.exports = { interface, deploy }
