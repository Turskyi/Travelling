import {
  initializeTestEnvironment,
  RulesTestEnvironment,
} from '@firebase/rules-unit-testing';
import * as fs from 'fs';
import * as path from 'path';

let testEnv: RulesTestEnvironment;

beforeAll(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: 'travelling-the-countries',
    firestore: {
      rules: fs.readFileSync(path.resolve(__dirname, 'firestore.rules'), 'utf8'),
    },
  });
});

afterAll(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

describe('Firestore Security Rules Isolation', () => {
  it('allows user A to read/write their own backup', async () => {
    const db = testEnv.authenticatedContext('userA').firestore();
    const backupRef = db.collection('user_backups').doc('userA');
    await expect(backupRef.set({ status: 'STAGED' })).resolves.toBeDefined();
    await expect(backupRef.get()).resolves.toBeDefined();
  });

  it('prevents user B from reading or writing user A backup', async () => {
    const db = testEnv.authenticatedContext('userB').firestore();
    const backupRef = db.collection('user_backups').doc('userA');
    await expect(backupRef.set({ status: 'HACKED' })).rejects.toThrow();
    await expect(backupRef.get()).rejects.toThrow();
  });
});
