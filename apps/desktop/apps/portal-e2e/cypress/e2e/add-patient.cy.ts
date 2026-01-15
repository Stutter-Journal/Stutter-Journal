describe('add patient', () => {
  it('connects after the patient list grows', () => {
    cy.clock();

    cy.intercept('GET', '**/api/doctor/me', {
      statusCode: 200,
      body: {
        id: 'doc_1',
        practiceId: 'practice_1',
        displayName: 'Dr Test',
        email: 'dr.test@example.com',
        role: 'doctor',
      },
    }).as('me');

    cy.intercept('POST', '**/api/links/pairing-code', {
      statusCode: 200,
      body: {
        code: '123456',
        qrText: '123456',
        expiresAt: new Date(Date.now() + 60_000).toISOString(),
      },
    }).as('pairingCode');

    let patientsCalls = 0;
    cy.intercept('GET', /\/api\/patients.*/, (req) => {
      patientsCalls += 1;

      // 1st call: baseline count
      // 2nd call: immediate poll (startWith(0))
      // 3rd+ call: simulate the patient having connected
      const connected = patientsCalls >= 3;

      req.reply({
        statusCode: 200,
        body: {
          patients: connected ? [{}] : [],
        },
      });
    }).as('patients');

    cy.visit('/app/patients');
    cy.wait('@me');

    cy.contains('button', 'Add patient').click();

    cy.wait('@pairingCode');
    cy.get('input[readonly][type="text"]').should('have.value', '123456');

    cy.contains('Waiting for the patient to connect…').should('be.visible');

    // Trigger the next poll. The 3rd /patients call returns a larger list.
    cy.tick(1000);

    cy.contains('Patient connected. Closing shortly.').should('be.visible');

    // Dialog auto-closes after 3 seconds once connected.
    cy.tick(3000);

    cy.contains('h3', 'Add patient').should('not.exist');
  });
});
