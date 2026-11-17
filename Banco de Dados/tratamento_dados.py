import gspread
import pandas as pd
import os
from dotenv import load_dotenv
load_dotenv()
cred = gspread.service_account(filename=os.getenv("google_cred"))


planilha_crua = cred.open_by_url("https://docs.google.com/spreadsheets/d/1JbIMgaeDPmRR02WwVuBkFPdsEhCMWRXkSIC_vY3JSt4/edit?gid=882887117#gid=882887117").\
worksheet("Anticoncepcionais")

planilha_out_dados = cred.open_by_url("https://docs.google.com/spreadsheets/d/1JbIMgaeDPmRR02WwVuBkFPdsEhCMWRXkSIC_vY3JSt4/edit?gid=882887117#gid=882887117").\
worksheet("Prin Ativo e Dias")

# Tratando dulpicadas e remedios formatados
import re, unicodedata

def padronizar(nome):
    nome = re.sub(r"\s*\+\s*", " + ", nome)
    nome = re.sub(r"\s+", " ", nome)
    return nome.strip()

def normalizar(nome):
    nome = unicodedata.normalize("NFKD", nome).encode("ascii", "ignore").decode()
    return nome.lower()

correcoes = {"DROSPERINONA": "DROSPIRENONA", "CLOMARDINONA": "CLORMADINONA", "ETINILDESTRADIOL": "ETINILESTRADIOL"}

def corrigir(nome):
    for errado, certo in correcoes.items():
        nome = nome.replace(errado, certo)
    return nome

# trazendo so medicamentos ativos
medicamentos_att = []

for cel in planilha_crua.get_all_records():
    if cel["SITUACAO_REGISTRO"] == "Ativo":
        nome_tratado = nome = padronizar(corrigir(cel["NOME_PRODUTO"]))
        if nome_tratado not in medicamentos_att:
            medicamentos_att.append(nome_tratado)

for nome in medicamentos_att:
    busca = normalizar(nome)

import sqlite3
con = sqlite3.connect(r"../app/src/main/assets/cicli.db")
cur = con.cursor()

# trazendo dados de dias e prin ativos
dias = {}
for linha in planilha_out_dados.get_all_records():
    chave = normalizar(padronizar(corrigir(linha["med_name"])))
    dias[chave] = linha

# Banco
# Adicionamos as informações do anticoncepcionais aqui
'''
for nome in medicamentos_att:
    cur.execute("INSERT INTO birth_pill (med_name, med_name_search, active_days, placebo_days, pause_days) VALUES (?, ?, 0, 0, 0)", (nome, nome.lower()))

atualizados = 0
for nome in medicamentos_att:
    busca = normalizar(nome)
    info = dias.get(busca)
    if info is None or info["active_days"] == "":
        print("Sem dados:", nome)
        continue
    atraso = info["miss_hours"] if info["miss_hours"] != "" else None
    cur.execute("UPDATE birth_pill SET med_name_search = ?, active_days = ?, placebo_days = ?, pause_days = ?, miss_hours = ?, source_date = ? WHERE med_name = ?", (busca, info["active_days"], info["placebo_days"], info["pause_days"], atraso, "2026-10-21", nome))
    atualizados += cur.rowcount
print(atualizados, "atualizados")
'''

# tratando principios ativos'
'''
ajuste_principio = {"levonorgestrel micronizado": "levonorgestrel", "clatrato de etinilestradiol betaciclodextrina": "etinilestradiol", "celulose microcristalina": None}

ingredientes = {}
for nome in medicamentos_att:
    info = dias.get(normalizar(nome))
    if info is None or info["active_days"] == "":
        continue
    pill = cur.execute("SELECT id FROM birth_pill WHERE med_name = ?", (nome,)).fetchone()
    if pill is None:
        continue
    for principio in info["principio_ativo"].split(","):
        principio = principio.strip().lower()
        principio = ajuste_principio.get(principio, principio)
        if principio is None:
            continue
        busca = normalizar(principio)
        if busca not in ingredientes:
            cur.execute("INSERT INTO active_ingredient (name, name_search) VALUES (?, ?)", (principio, busca))
            ingredientes[busca] = cur.lastrowid
        cur.execute("INSERT OR IGNORE INTO birth_pill_ingredient (birth_pill_id, ingredient_id) VALUES (?, ?)", (pill[0], ingredientes[busca]))
print(len(ingredientes), "princípios ativos")
'''

# tratando remedios que cortam o efeito

planilha_med_avoid = planilha_crua = cred.open_by_url("https://docs.google.com/spreadsheets/d/1JbIMgaeDPmRR02WwVuBkFPdsEhCMWRXkSIC_vY3JSt4/edit?gid=882887117#gid=882887117").\
worksheet("med to avoid")

for cel in planilha_med_avoid.get_all_records():
    cur.execute("INSERT INTO medicine_to_avoid (name, name_search, why) VALUES (?, ?, ?)", (cel["name"], cel["name_search"], cel["why"]))

con.commit()
con.close()

print("commitado!")