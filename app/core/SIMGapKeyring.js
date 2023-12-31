import { EventEmitter } from 'events';
import { ObservableStore } from '@metamask/obs-store';
import {
  CryptoHDKey,
  CryptoAccount,
  ETHSignature,
  EthSignRequest,
  DataType,
} from '@keystonehq/bc-ur-registry-eth';
import { stringify, v4 } from 'uuid';
import { TransactionFactory } from '@ethereumjs/tx';
import {
  publicToAddress,
  toChecksumAddress,
  rlphash,
  addHexPrefix,
  BN,
  stripHexPrefix,
} from 'ethereumjs-util';
import { Transaction } from '@ethereumjs/tx';
import { FeeMarketEIP1559Transaction } from '@ethereumjs/tx'
import { utils as ethersUtils } from 'ethers';

import SIMGapWallet from './SIMGapWallet';
import Logger from '../util/Logger';
import { PermissionsAndroid, DeviceEventEmitter, Modal } from 'react-native';

const keyringType = 'SIMGap Wallet Device';

var _initListeners = false;

class SIMGapKeyring {
  constructor(opts) {
    // @ts-ignore
    this.version = 1;
    this.type = keyringType;
    this.getName = () => {
      return this.name;
    };

    this.page = 0;
    this.perPage = 5;
    this.accs = [];
    this.accounts = [];
    this.currentAccount = 0;
    this.unlockedAccount = 0;
    this.name = 'SIMGap Hardware';
    this.initialized = false; //hd props;

    this.deserialize(opts);

    this.setAccountToUnlock = (index) => {
      this.unlockedAccount = parseInt(index, 10);
    };

    if (SIMGapKeyring.instance) {
      SIMGapKeyring.instance.deserialize(opts);
      return SIMGapKeyring.instance;
    }

    SIMGapKeyring.instance = this;
    DeviceEventEmitter.addListener('EVENT_SIMGAP_LOG', (msg) => {
      Logger.log('SIMGapWallet Log: ' + msg);
    });
    DeviceEventEmitter.addListener('EVENT_SIMGAP_ERROR', (msg) => {
      Logger.log('SIMGapWallet Error: ' + msg);
    });
  }

  // exportAccount should return a hex-encoded private key:
  exportAccount(address, opts = {}) {
    Logger.log('SIMGapKeyring.js: exportAccount()');
    throw new Error(`Private key export is not supported.`);
  }

  // removeAccount
  removeAccount(address) {
    Logger.log('SIMGapKeyring.js: removeAccount()');
    if (
      !this.accounts.map((a) => a.toLowerCase()).includes(address.toLowerCase())
    ) {
      throw new Error(`Address ${address} not found in this keyring`);
    }

    this.accounts = this.accounts.filter(
      (a) => a.toLowerCase() !== address.toLowerCase(),
    );
  }

  // addAccounts
  async addAccounts(n = 1) {
    Logger.log('SIMGapKeyring.js: addAccounts()');
    const from = this.unlockedAccount;
    const to = from + n;
    const newAccounts = [];

    for (let i = from; i < to; i++) {
      //      const address = await OTIColdWallet.newWallet();
      const address = this.accs[i].address;
      newAccounts.push(address);
      this.page = 0;
      this.unlockedAccount++;
    }

    Logger.log('=== Add ' + newAccounts + ' accounts to ' + this.accounts);
    this.accounts = this.accounts.concat(newAccounts);
    Logger.log('=== Now accounts is ' + this.accounts);
    return this.accounts;
  }

  // getAccounts
  getAccounts() {
    Logger.log('SIMGapKeyring.js: getAccounts(): ' + this.accounts);
    return Promise.resolve(this.accounts);
  }

  // signTransaction
  // tx is an instance of the ethereumjs-transaction class.
  async signTransaction(address, tx) {
    // It also works...
    // const message = tx.getMessageToSign(true);
    // const serializedMessage = ethersUtils.hexlify(message);
    // Logger.log('SIMGapKeyring.js: signTransaction() serializedMessage:', serializedMessage);

    const messageToSign = tx.getMessageToSign(true);

    let rawTxHex = Buffer.isBuffer(messageToSign)
      ? messageToSign.toString('hex')
      : ethUtil.rlp.encode(messageToSign).toString('hex');

    // Logger.log('SIMGapKeyring.js: signTransaction() rawTxHex:', rawTxHex);

    // Logger.log('SIMGapKeyring.js: signTransaction() using serializedMessage');

    const sig = await SIMGapWallet.signTransaction(
      address,
      tx.type,
      rawTxHex,
      // serializedMessage, // It also works !!!
      tx.common.chainId(),
    );

    Logger.log('SIMGapKeyring.js: tx.signTransaction() v: ' + sig.v.toString('hex'));
    Logger.log('SIMGapKeyring.js: tx.signTransaction() s: ' + sig.s.toString('hex'));
    Logger.log('SIMGapKeyring.js: tx.signTransaction() r: ' + sig.r.toString('hex'));

    // const privateKey = Buffer.from('59c4a7a81b26490ec4f3d905746f899a2302ec6a5fd362e6e630d5c2139ad898', 'hex');
    // const signedTx = tx.sign(privateKey);
    // const txJson = signedTx.toJSON();
    // Newer versions of Ethereumjs-tx are immutable and return a new tx object
    // return Promise.resolve(signedTx1 === undefined ? tx : signedTx1);
    
    const txData = tx.toJSON();
    // The fromTxData utility expects a type to support transactions with a type other than 0
    txData.type = tx.type;
    // The fromTxData utility expects v,r and s to be hex prefixed
    txData.v = addHexPrefix(sig.v.toString('hex'));
    txData.r = addHexPrefix(sig.r.toString('hex'));
    txData.s = addHexPrefix(sig.s.toString('hex'));
    txData.type = tx.type;
    // Adopt the 'common' option from the original transaction and set the
    // returned object to be frozen if the original is frozen.
    return TransactionFactory.fromTxData(txData, {
      common: tx.common,
    });
  }

  // signMessage
  async signMessage(withAccount, data) {
    Logger.log('SIMGapKeyring.js: signMessage()');
    const { r, s, v } = await SIMGapWallet.signMessage(withAccount, data);
    return '0x' + Buffer.concat([r, s, v]).toString('hex');
  }

  // signPersonalMessage
  async signPersonalMessage(withAccount, messageHex) {
    Logger.log('SIMGapKeyring.js: signPersonalMessage()');
    const { r, s, v } = await SIMGapWallet.signPersonalMessage(
      withAccount,
      messageHex,
    );
    return '0x' + Buffer.concat([r, s, v]).toString('hex');
  }

  // signTypedData
  async signTypedData(withAccount, typedData) {
    Logger.log('SIMGapKeyring.js: signTypedData()');
    const { r, s, v } = await SIMGapWallet.signTypedData(
      withAccount,
      Buffer.from(JSON.stringify(typedData), 'utf-8'),
    );
    return '0x' + Buffer.concat([r, s, v]).toString('hex');
  }

  // decryptMessage
  // For eth_decryptMessage:
  async decryptMessage(withAccount, encryptedData) {
    Logger.log('SIMGapKeyring.js: decryptMessage()');
    const dec = await SIMGapWallet.signTypedData(withAccount, encryptedData);
    return dec;
  }

  // getEncryptionPublicKey
  // get public key for nacl
  async getEncryptionPublicKey(withAccount) {
    Logger.log('SIMGapKeyring.js: getEncryptionPublicKey()');
    return await SIMGapWallet.getEncryptionPublicKey(withAccount);
  }

  // getAppKeyAddress

  serialize() {
    Logger.log('SIMGapKeyring.js: serialize()');
    return Promise.resolve({
      //common
      initialized: this.initialized,
      page: this.page,
      perPage: this.perPage,
      accs: this.accs,
      accounts: this.accounts,
      currentAccount: this.currentAccount,
      name: this.name,
      version: this.version,
    });
  }

  deserialize(opts) {
    if (opts) {
      //common props;
      this.page = opts.page;
      this.perPage = opts.perPage;
      this.accs = opts.accs;
      this.accounts = opts.accounts;
      this.currentAccount = opts.currentAccount;
      this.name = opts.name;
      this.initialized = opts.initialized;
      Logger.log('SIMGapKeyring.js: deserialize() accounts: ' + this.accounts);
    } else {
      Logger.log('SIMGapKeyring.js: deserialize() no opts');
    }
  }

  getFirstPage() {
    Logger.log('SIMGapKeyring.js: getFirstPage()');
    this.page = 0;
    return this.__getPage(1);
  }

  getNextPage() {
    Logger.log('SIMGapKeyring.js: getNextPage()');
    return this.__getPage(1);
  }

  getPreviousPage() {
    Logger.log('SIMGapKeyring.js: getPreviousPage()');
    return this.__getPage(-1);
  }

  async __getPage(increment) {
    if (!this.initialized) {
      //      await this.readKeyring();

      this.accs = await SIMGapWallet.fetchResult(1); //SIMGapWallet.available();
      Logger.log(
        '...............this.accs is ' +
        (this.accs == null ? '' : 'NOT ') +
        'null.',
      );
      if (this.accs == null) return [];
      Logger.log('---------------this.accs is ' + this.accs);
      this.initialized = true;
    }

    this.page += increment;

    if (this.page <= 0) {
      this.page = 1;
    }

    var i = (this.page - 1) * this.perPage;
    var to = i + this.perPage;
    const accounts = [];

    if (i >= this.accs.length) return accounts;
    if (to > this.accs.length) to = this.accs.length;

    while (i < to) {
      const address = this.accs[i].address;
      accounts.push({
        address,
        balance: null,
        index: i,
      });
      //        this.indexes[toChecksumAddress(address)] = i;
      i++;
    }
    Logger.log('this.page: ' + this.page);
    Logger.log('accounts: ' + accounts);
    Logger.log('this.accs: ' + this.accs);

    return accounts;
  }
}

SIMGapKeyring.type = keyringType;

export { SIMGapKeyring };
