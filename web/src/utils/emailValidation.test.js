import { test } from 'node:test'
import assert from 'node:assert/strict'
import { validateEmailFormat, getEmailTypoSuggestion } from './emailValidation.js'

test('valid gmail address passes', () => {
  const result = validateEmailFormat('someone@gmail.com')
  assert.equal(result.valid, true)
  assert.equal(result.message, null)
})

test('blank email is rejected', () => {
  assert.equal(validateEmailFormat('').valid, false)
  assert.equal(validateEmailFormat('   ').valid, false)
})

test('email with embedded spaces is rejected', () => {
  assert.equal(validateEmailFormat('some one@gmail.com').valid, false)
})

test('missing @ is rejected', () => {
  assert.equal(validateEmailFormat('someone.gmail.com').valid, false)
})

test('multiple @ signs are rejected', () => {
  assert.equal(validateEmailFormat('some@one@gmail.com').valid, false)
})

test('leading or trailing dot in local part is rejected', () => {
  assert.equal(validateEmailFormat('.someone@gmail.com').valid, false)
  assert.equal(validateEmailFormat('someone.@gmail.com').valid, false)
})

test('consecutive dots are rejected', () => {
  assert.equal(validateEmailFormat('some..one@gmail.com').valid, false)
  assert.equal(validateEmailFormat('someone@gmail..com').valid, false)
})

test('domain missing a TLD is rejected', () => {
  assert.equal(validateEmailFormat('someone@gmail').valid, false)
})

test('non-alphabetic TLD is rejected', () => {
  assert.equal(validateEmailFormat('someone@gmail.123').valid, false)
})

test('address longer than 254 characters is rejected', () => {
  const longLocal = 'a'.repeat(250)
  assert.equal(validateEmailFormat(`${longLocal}@gmail.com`).valid, false)
})

test('subdomains and plus-addressing are accepted', () => {
  assert.equal(validateEmailFormat('user+tag@mail.example-provider.com').valid, true)
})

test('known domain typos produce a corrected suggestion', () => {
  assert.equal(getEmailTypoSuggestion('someone@gmial.com'), 'someone@gmail.com')
  assert.equal(getEmailTypoSuggestion('someone@gnail.com'), 'someone@gmail.com')
  assert.equal(getEmailTypoSuggestion('someone@gmai.com'), 'someone@gmail.com')
  assert.equal(getEmailTypoSuggestion('someone@yahooo.com'), 'someone@yahoo.com')
  assert.equal(getEmailTypoSuggestion('someone@hotmial.com'), 'someone@hotmail.com')
  assert.equal(getEmailTypoSuggestion('someone@outlok.com'), 'someone@outlook.com')
})

test('typo suggestion preserves the local part exactly, including case', () => {
  assert.equal(getEmailTypoSuggestion('John.Doe+tag@gmial.com'), 'John.Doe+tag@gmail.com')
})

test('no suggestion for a correctly-spelled domain', () => {
  assert.equal(getEmailTypoSuggestion('someone@gmail.com'), null)
})

test('no suggestion when there is no @ or the input is empty', () => {
  assert.equal(getEmailTypoSuggestion(''), null)
  assert.equal(getEmailTypoSuggestion('not-an-email'), null)
  assert.equal(getEmailTypoSuggestion(null), null)
})
