package nl.tudelft.serg.evosql.metaheuristics;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nl.tudelft.serg.evosql.fixture.Fixture;
import nl.tudelft.serg.evosql.fixture.FixtureTable;
import nl.tudelft.serg.evosql.sql.TableSchema;
import nl.tudelft.serg.evosql.util.random.Randomness;

public abstract class Approach {
	protected static Logger log = LogManager.getLogger(Approach.class);
	protected static Randomness random = new Randomness();
	
	protected int generations = 0;
	protected int individualCount = 0;
	

	/** Path under test**/
	protected String pathToTest;

	protected Map<String, TableSchema> tableSchemas;
	
	protected String exceptions;
	
	public Approach(Map<String, TableSchema> pTableSchemas, String pPathToBeTested) {
		this.tableSchemas = pTableSchemas; 
		this.pathToTest = pPathToBeTested;
		this.exceptions = "";
	}

	public Fixture execute(long pathTime) throws SQLException {
		throw new RuntimeException("Approach did not implement execute.");
	}
	
	public int getGenerations() {
		return generations;
	}

	public int getIndividualCount() {
		return individualCount;
	}
	
	public String getExceptions() {
		return exceptions;
	}
	
	public Map<String, TableSchema> getTableSchemas() {
		return tableSchemas;
	}
	
	protected Fixture minimize (Fixture fixture) throws SQLException {
		Fixture minimizedFixture = fixture.copy();
		
		if (!hasOutput(minimizedFixture))
			return minimizedFixture;

		// Loop over all tables
		for (FixtureTable ft : minimizedFixture.getTables()) {
			// Go over all rows, test them without this row and if hasOutput becomes true remove row
			for (int i = 0; i < ft.getRowCount() && ft.getRowCount() > 1 /*At least 1 row per table remains*/; i++) {
				if (hasOutput(minimizedFixture, ft.getName(), i)) {
					minimizedFixture.remove(ft.getName(), i);
					i--;
				}
			}	
		}
		
		return minimizedFixture;
	}
	
	public boolean hasOutput(Fixture fixture) throws SQLException {
		return hasOutput(fixture, "", -1);
	}
	
	protected boolean hasOutput (Fixture fixture, String excludeTableName, int excludeIndex) throws SQLException {
		// Truncate tables in Instrumented DB
		for (TableSchema tableSchema : tableSchemas.values()) {
			genetic.Instrumenter.execute(tableSchema.getTruncateSQL());
		}
		
		// Insert population
		for (String sqlStatement : fixture.getInsertStatements(excludeTableName, excludeIndex)) {
			genetic.Instrumenter.execute(sqlStatement);
		}
		
		Statement st = genetic.Instrumenter.getStatement();

		try {
			ResultSet res = st.executeQuery(pathToTest);
			if (res.next()) // If next returns true there is at least one row.
				return true;
			else
				return false;
		} catch (SQLException e) {
			if (!exceptions.contains(e.getMessage())) {
				exceptions += ", " + e.getMessage();
			}
			log.error(e.toString());
			return false;
		}
	}
}
