const fs = require('fs');
const solc = require('solc')
const Web3 = require('web3');
const web3 = new Web3();

web3.setProvider(new web3.providers.HttpProvider('http://localhost:8545'));

const code = fs.readFileSync('src/main/solidity/Pong.sol').toString();
const compiledCode = solc.compile(code);

const abiDefinition = JSON.parse(compiledCode.contracts[':Pong'].interface);
const byteCode = compiledCode.contracts[':Pong'].bytecode;

const PongContract = web3.eth.contract(abiDefinition);

const leftPC = PongContract.new(800,600,80,15,15,1,3,0,{
  data: byteCode,
  from: web3.eth.accounts[0],
  gas: 4700000}, (err1, left) => {
    if (left.side !== undefined) {
      const rightContract = PongContract.new(800,600,80,15,15,1,3,1,{
        data: byteCode,
        from: web3.eth.accounts[0],
        gas: 4700000}, (err2, right) => {
          if (right.side !== undefined) {
            left.introduce.call(right.address, console.log);
            right.introduce.call(left.address, console.log);
          }
        });
    }
  });
