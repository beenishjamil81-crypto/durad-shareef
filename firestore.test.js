const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const ADMIN_UID = "admin_789";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated: cannot read or write recitations", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("recitations").get());
  await assertFails(unauthDb.collection("recitations").doc("rec1").set({
    userId: "anon",
    userName: "Anon",
    count: 10,
    duroodType: "Durood Ibrahimi",
    isPublic: true,
    createdAt: new Date(),
  }));
});

test("Authenticated: can create valid recitation for self", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("recitations").doc("rec_alice").set({
    userId: ALICE_UID,
    userName: "Alice",
    count: 100,
    duroodType: "Durood Ibrahimi",
    isPublic: true,
    createdAt: new Date(),
  }));
});

test("Authenticated: cannot create recitation with mismatched userId", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("recitations").doc("rec_spoof").set({
    userId: BOB_UID,
    userName: "Bob",
    count: 100,
    duroodType: "Durood Ibrahimi",
    isPublic: true,
    createdAt: new Date(),
  }));
});

test("Authenticated: cannot create recitation with ghost fields", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("recitations").doc("rec_ghost").set({
    userId: ALICE_UID,
    userName: "Alice",
    count: 100,
    duroodType: "Durood Ibrahimi",
    isPublic: true,
    createdAt: new Date(),
    ghostField: "hacker",
  }));
});

test("Authenticated: can query public recitations", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("recitations").doc("rec_seeded").set({
      userId: BOB_UID,
      userName: "Bob",
      count: 33,
      duroodType: "Durood Ibrahimi",
      isPublic: true,
      createdAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("recitations").where("isPublic", "==", true).get()
  );
});

test("Cross-user: Bob cannot delete Alice's recitation", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("recitations").doc("rec_alice_1").set({
      userId: ALICE_UID,
      userName: "Alice",
      count: 33,
      duroodType: "Durood Ibrahimi",
      isPublic: true,
      createdAt: new Date(),
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("recitations").doc("rec_alice_1").delete());
});

test("Admin: can delete any recitation", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("recitations").doc("rec_to_delete").set({
      userId: ALICE_UID,
      userName: "Alice",
      count: 50,
      duroodType: "Durood Ibrahimi",
      isPublic: true,
      createdAt: new Date(),
    });
  });

  const adminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: "beenishjamil81@gmail.com",
    email_verified: true,
  }).firestore();

  await assertSucceeds(adminDb.collection("recitations").doc("rec_to_delete").delete());
});

test("Summary: signed in user can read and update global summary", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("summary").doc("global").set({
    totalCount: 500,
    totalSubmissions: 5,
    updatedAt: new Date(),
  }));

  await assertSucceeds(aliceDb.collection("summary").doc("global").get());
});
