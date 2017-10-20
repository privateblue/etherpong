const Web3 = require('web3');

const web3 = new Web3();
web3.setProvider(new web3.providers.HttpProvider('http://localhost:8545'));
const owner = web3.eth.accounts[0];
const iface =
  '[{"constant":false,"inputs":[{"name":"_opponent","type":"address"}],"name":"introduce","outputs":[{"name":"","type":"bool"}],"payable":false,"stateMutability":"nonpayable","type":"function"},{"constant":true,"inputs":[],"name":"height","outputs":[{"name":"","type":"int16"}],"payable":false,"stateMutability":"view","type":"function"},{"constant":false,"inputs":[{"name":"px","type":"int256"},{"name":"py","type":"int256"},{"name":"vx","type":"int256"},{"name":"vy","type":"int256"}],"name":"updateWith","outputs":[],"payable":false,"stateMutability":"nonpayable","type":"function"},{"constant":true,"inputs":[],"name":"startSide","outputs":[{"name":"","type":"uint8"}],"payable":false,"stateMutability":"view","type":"function"},{"constant":true,"inputs":[],"name":"paddleLength","outputs":[{"name":"","type":"int16"}],"payable":false,"stateMutability":"view","type":"function"},{"constant":true,"inputs":[],"name":"lastUpdatedAt","outputs":[{"name":"","type":"uint256"}],"payable":false,"stateMutability":"view","type":"function"},{"constant":true,"inputs":[],"name":"paddlePos","outputs":[{"name":"","type":"int16"}],"payable":false,"stateMutability":"view","type":"function"},{"constant":true,"inputs":[],"name":"side","outputs":[{"name":"","type":"uint8"}],"payable":false,"stateMutability":"view","type":"function"},{"constant":true,"inputs":[],"name":"opponent","outputs":[{"name":"","type":"address"}],"payable":false,"stateMutability":"view","type":"function"},{"constant":true,"inputs":[],"name":"maxBallSpeed","outputs":[{"name":"","type":"uint16"}],"payable":false,"stateMutability":"view","type":"function"},{"constant":true,"inputs":[],"name":"width","outputs":[{"name":"","type":"int16"}],"payable":false,"stateMutability":"view","type":"function"},{"constant":false,"inputs":[],"name":"update","outputs":[],"payable":false,"stateMutability":"nonpayable","type":"function"},{"constant":true,"inputs":[],"name":"ballSize","outputs":[{"name":"","type":"int16"}],"payable":false,"stateMutability":"view","type":"function"},{"constant":true,"inputs":[],"name":"paddleWidth","outputs":[{"name":"","type":"int16"}],"payable":false,"stateMutability":"view","type":"function"},{"constant":true,"inputs":[],"name":"running","outputs":[{"name":"","type":"bool"}],"payable":false,"stateMutability":"view","type":"function"},{"constant":true,"inputs":[],"name":"ballPos","outputs":[{"name":"x","type":"int256"},{"name":"y","type":"int256"}],"payable":false,"stateMutability":"view","type":"function"},{"constant":true,"inputs":[],"name":"minBallSpeed","outputs":[{"name":"","type":"uint16"}],"payable":false,"stateMutability":"view","type":"function"},{"constant":true,"inputs":[],"name":"ballVel","outputs":[{"name":"x","type":"int256"},{"name":"y","type":"int256"}],"payable":false,"stateMutability":"view","type":"function"},{"constant":true,"inputs":[],"name":"score","outputs":[{"name":"","type":"uint16"}],"payable":false,"stateMutability":"view","type":"function"},{"inputs":[{"name":"_width","type":"int16"},{"name":"_height","type":"int16"},{"name":"_paddleLength","type":"int16"},{"name":"_paddleWidth","type":"int16"},{"name":"_ballSize","type":"int16"},{"name":"_minBallSpeed","type":"uint16"},{"name":"_maxBallSpeed","type":"uint16"},{"name":"_side","type":"uint8"}],"payable":false,"stateMutability":"nonpayable","type":"constructor"}]'
const abiDefinition = JSON.parse(iface);
const PongContract = web3.eth.contract(abiDefinition);

function init(leftAddress, rightAddress) {
  const left = PongContract.at(leftAddress);
  const right = PongContract.at(rightAddress);
  left.introduce(rightAddress, {from: owner, gas: 4700000}, function(error, result) {
    console.log(rightAddress + " introduced to Left");
  });
  right.introduce(leftAddress, {from: owner, gas: 4700000}, function(error, result) {
    console.log(leftAddress + " introduced to Right");
  });
}

function update(leftAddress, rightAddress, interval) {
  const left = PongContract.at(leftAddress);
  const right = PongContract.at(rightAddress);
  setInterval(function() {
    const n = web3.eth.blockNumber;
    console.log(n + ". Left running: " + left.running());
    console.log(n + ". Right running: " + right.running());
    console.log(n + ". Left last updated at: " + left.lastUpdatedAt().toNumber());
    console.log(n + ". Right last updated at: " + right.lastUpdatedAt().toNumber());
    console.log(n + ". Left ball position: " + left.ballPos().map(n => n.toNumber()));
    console.log(n + ". Right ball position: " + right.ballPos().map(n => n.toNumber()));
    console.log(n + ". Left ball velocity: " + left.ballVel().map(n => n.toNumber()));
    console.log(n + ". Right ball velocity: " + right.ballVel().map(n => n.toNumber()));
    console.log(n + ". Left score: " + left.score().toNumber());
    console.log(n + ". Right score: " + right.score().toNumber());
    console.log(n + ". Left paddle: " + left.paddlePos().toNumber());
    console.log(n + ". Right paddle: " + right.paddlePos().toNumber());
    console.log("Updating...\n")
    left.update({from: owner, gas: 4700000});
  }, interval);
}

function config(leftAddress, rightAddress) {
  var left = PongContract.at(leftAddress);
  var right = PongContract.at(rightAddress);
  return {
    width: left.width().toNumber(),
    height: left.height().toNumber(),
    paddleLength: left.paddleLength().toNumber(),
    paddleWidth: left.paddleWidth().toNumber(),
    ballSize: left.ballSize().toNumber(),
    minBallSpeed: left.minBallSpeed().toNumber(),
    maxBallSpeed: left.maxBallSpeed().toNumber()
  };
}

function onBlock(leftAddress, rightAddress, callback) {
  var left = PongContract.at(leftAddress);
  var right = PongContract.at(rightAddress);
  var filter = web3.eth.filter('latest');
  filter.watch(function (error, blockHash) {
    if (!error) {
      callback(
        web3.eth.blockNumber,
        left.running(),
        right.running(),
        left.lastUpdatedAt().toNumber(),
        right.lastUpdatedAt().toNumber(),
        left.ballPos().map(n => n.toNumber()),
        right.ballPos().map(n => n.toNumber()),
        left.ballVel().map(n => n.toNumber()),
        right.ballVel().map(n => n.toNumber()),
        left.score().toNumber(),
        right.score().toNumber(),
        left.paddlePos().toNumber(),
        right.paddlePos().toNumber()
      );
    }
  });
}

module.exports = { init, update, onBlock };