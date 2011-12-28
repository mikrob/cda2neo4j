launch com.octo.cda2neo4j.Cda2Graph.main() with parameters : -in file.odem -out outfolder

file.odem has to be put at the root of the project
This parse the XML and insert all node in neo4j with correct relation betweens.

Then 
Unit test QueriesTests are queries example

findOrpheanNodes find all node without relationships (orphean node)
findNodesWithNoEntranceLinks find all node without incoming relationships (node never used)