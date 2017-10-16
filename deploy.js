const fs = require('fs');
const solc = require('solc')
const Web3 = require('web3');
const web3 = new Web3();

web3.setProvider(new web3.providers.HttpProvider('http://localhost:8545'));

const owner = web3.eth.accounts[0];

const code = fs.readFileSync('src/main/solidity/Pong.sol').toString();
const compiledCode = solc.compile(code);

const abiDefinition = JSON.parse(compiledCode.contracts[':Pong'].interface);
const byteCode = compiledCode.contracts[':Pong'].bytecode;

const PongContract = web3.eth.contract(abiDefinition);

var left;
var right;

const leftPC = PongContract.new(800,600,80,15,15,1,3,0,{
  data: byteCode,
  from: owner,
  gas: 4700000}, (err, contract) => {
    if (contract.side !== undefined) {
      left = contract;
      console.log("Left address: " + contract.address);
    }
  });

const rightPC = PongContract.new(800,600,80,15,15,1,3,1,{
  data: byteCode,
  from: owner,
  gas: 4700000}, (err, contract) => {
    if (contract.side !== undefined) {
      right = contract;
      console.log("Right address: " + contract.address);
    }
  });
