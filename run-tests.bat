@echo off
setlocal
cd /d c:\Users\Dell\Documents\onlinebanking
if not exist test-results mkdir test-results

set BASE=http://localhost:8080
set DAVE_ACCT=3985185278
set BOB_ACCT=2489803977
set DAVE_ID=14
set BOB_ID=11

for /f "delims=" %%t in (token-dave.txt) do set DAVE=%%t
for /f "delims=" %%t in (token-bob.txt) do set BOB=%%t
for /f "delims=" %%t in (token-admin.txt) do set ADMIN=%%t

set R=test-results

echo ========================
echo PHASE 3 - TRANSACTIONS
echo ========================

curl -s -o %R%\t1-tx-dave-own.json -w "%%{http_code}" -H "Authorization: Bearer %DAVE%" %BASE%/api/transactions/%DAVE_ACCT% > %R%\t1.code
curl -s -o %R%\t2-tx-dave-bobacct.json -w "%%{http_code}" -H "Authorization: Bearer %DAVE%" %BASE%/api/transactions/%BOB_ACCT% > %R%\t2.code
curl -s -o %R%\t3-tx-admin-bobacct.json -w "%%{http_code}" -H "Authorization: Bearer %ADMIN%" %BASE%/api/transactions/%BOB_ACCT% > %R%\t3.code
curl -s -o %R%\t4-tx-noauth.json -w "%%{http_code}" %BASE%/api/transactions/%DAVE_ACCT% > %R%\t4.code

echo ========================
echo PHASE 4 - PROFILE
echo ========================

curl -s -o %R%\p1-me-dave.json -w "%%{http_code}" -H "Authorization: Bearer %DAVE%" %BASE%/api/customers/me > %R%\p1.code
curl -s -o %R%\p2-get-own.json -w "%%{http_code}" -H "Authorization: Bearer %DAVE%" %BASE%/api/customers/%DAVE_ID% > %R%\p2.code
curl -s -o %R%\p3-get-bob-as-dave.json -w "%%{http_code}" -H "Authorization: Bearer %DAVE%" %BASE%/api/customers/%BOB_ID% > %R%\p3.code
curl -s -o %R%\p4-put-me-dave.json -w "%%{http_code}" -X PUT -H "Authorization: Bearer %DAVE%" -H "Content-Type: application/json" --data-binary @payload-update-dave.json %BASE%/api/customers/me > %R%\p4.code
curl -s -o %R%\p5-put-bob-as-dave.json -w "%%{http_code}" -X PUT -H "Authorization: Bearer %DAVE%" -H "Content-Type: application/json" --data-binary @payload-update-dave.json %BASE%/api/customers/%BOB_ID% > %R%\p5.code
curl -s -o %R%\p6-get-dave-as-admin.json -w "%%{http_code}" -H "Authorization: Bearer %ADMIN%" %BASE%/api/customers/%DAVE_ID% > %R%\p6.code
curl -s -o %R%\p7-put-bob-as-admin.json -w "%%{http_code}" -X PUT -H "Authorization: Bearer %ADMIN%" -H "Content-Type: application/json" --data-binary @payload-update-dave.json %BASE%/api/customers/%BOB_ID% > %R%\p7.code

echo ============================
echo PHASE 4 - CHANGE PASSWORD
echo ============================

curl -s -o %R%\cp1-wrong-current.json -w "%%{http_code}" -X PUT -H "Authorization: Bearer %DAVE%" -H "Content-Type: application/json" --data-binary @payload-cp-wrong.json %BASE%/api/customers/me/password > %R%\cp1.code
curl -s -o %R%\cp2-mismatch.json -w "%%{http_code}" -X PUT -H "Authorization: Bearer %DAVE%" -H "Content-Type: application/json" --data-binary @payload-cp-mismatch.json %BASE%/api/customers/me/password > %R%\cp2.code
curl -s -o %R%\cp3-valid.json -w "%%{http_code}" -X PUT -H "Authorization: Bearer %DAVE%" -H "Content-Type: application/json" --data-binary @payload-cp-valid.json %BASE%/api/customers/me/password > %R%\cp3.code
curl -s -o %R%\cp4-login-newpw.json -w "%%{http_code}" -H "Content-Type: application/json" --data-binary @payload-login-dave-newpw.json %BASE%/api/auth/login > %R%\cp4.code
curl -s -o %R%\cp5-login-oldpw.json -w "%%{http_code}" -H "Content-Type: application/json" --data-binary @payload-login-dave-oldpw.json %BASE%/api/auth/login > %R%\cp5.code

echo ======================================
echo PRESERVED - DEPOSIT / WITHDRAW / TRANSFER
echo ======================================

curl -s -o %R%\f1-deposit-dave.json -w "%%{http_code}" -H "Authorization: Bearer %DAVE%" -H "Content-Type: application/json" --data-binary @payload-deposit.json %BASE%/api/accounts/deposit > %R%\f1.code
curl -s -o %R%\f2-deposit-dave-as-bob.json -w "%%{http_code}" -H "Authorization: Bearer %BOB%" -H "Content-Type: application/json" --data-binary @payload-deposit.json %BASE%/api/accounts/deposit > %R%\f2.code
curl -s -o %R%\f3-withdraw-dave.json -w "%%{http_code}" -H "Authorization: Bearer %DAVE%" -H "Content-Type: application/json" --data-binary @payload-withdraw.json %BASE%/api/accounts/withdraw > %R%\f3.code
curl -s -o %R%\f4-withdraw-dave-as-bob.json -w "%%{http_code}" -H "Authorization: Bearer %BOB%" -H "Content-Type: application/json" --data-binary @payload-withdraw.json %BASE%/api/accounts/withdraw > %R%\f4.code
curl -s -o %R%\f5-transfer-dave-to-bob.json -w "%%{http_code}" -H "Authorization: Bearer %DAVE%" -H "Content-Type: application/json" --data-binary @payload-transfer-dave-to-bob.json %BASE%/api/transfers > %R%\f5.code
curl -s -o %R%\f6-transfer-cross-as-dave.json -w "%%{http_code}" -H "Authorization: Bearer %DAVE%" -H "Content-Type: application/json" --data-binary @payload-transfer-cross.json %BASE%/api/transfers > %R%\f6.code

echo ========================
echo NEGATIVE AUTH / ROLE TESTS
echo ========================

curl -s -o %R%\n1-admin-as-admin.json -w "%%{http_code}" -H "Authorization: Bearer %ADMIN%" %BASE%/api/admin/stats > %R%\n1.code
curl -s -o %R%\n2-admin-as-dave.json -w "%%{http_code}" -H "Authorization: Bearer %DAVE%" %BASE%/api/admin/stats > %R%\n2.code
curl -s -o %R%\n3-bad-login.json -w "%%{http_code}" -H "Content-Type: application/json" --data-binary @payload-login-dave-oldpw.json %BASE%/api/auth/login > %R%\n3.code
curl -s -o %R%\n4-root-page.html -w "%%{http_code}" %BASE%/ > %R%\n4.code

echo DONE
endlocal