package uk.co.compendiumdev.thingifier.core;

import uk.co.compendiumdev.thingifier.core.domain.datapopulator.DataPopulator;
import uk.co.compendiumdev.thingifier.core.domain.definitions.Cardinality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.ERInstanceData;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceCollection;

import java.util.Collection;
import java.util.List;

/*
    The ERM has the 'model' (ERSchema) and the 'instances' (things).
    Schema and instances are separate to allow us to have multiple
    'databases' in memory at the same time built from the same schema.
 */
public class EntityRelModel {

    // TODO: this should be a Map so that key, database can be used
    // e.g. key from a 'session', or 'custom' or 'default'
    private final ERInstanceData database;
    private final ERSchema schema; // all the definitions

    public EntityRelModel(){
        schema = new ERSchema();
        database = new ERInstanceData();
    }

    public EntityRelModel(final ERSchema schema, final ERInstanceData erInstanceData) {
        this.schema = schema;
        this.database = erInstanceData;
    }

    public EntityDefinition createEntityDefinition(final String entityName, final String pluralName) {
        EntityDefinition defn = schema.defineEntity(entityName, pluralName);
        database.createInstanceCollectionFor(defn);
        return defn;
    }

    public ERSchema getSchema(){
        return schema;
    }

    public ERInstanceData getInstanceData(){
        return database;
    }

    // Schema methods
    public boolean hasEntityNamed(final String aName) {
        return schema.hasEntityNamed(aName);
    }

    public List<String> getEntityNames() {
        return schema.getEntityNames();
    }

    public Collection<RelationshipDefinition> getRelationshipDefinitions() {
        return schema.getRelationships();
    }

    public RelationshipDefinition createRelationshipDefinition(
            EntityDefinition from, EntityDefinition to, final String named, final Cardinality of) {
        return schema.defineRelationship(from, to, named, of);
    }

    public boolean hasRelationshipNamed(final String relationshipName) {
        return schema.hasRelationshipNamed(relationshipName);
    }

    public boolean hasEntityWithPluralNamed(final String term) {
        return schema.hasEntityWithPluralNamed(term);
    }

    public EntityDefinition getEntityDefinitionWithPluralNamed(final String term){
        return schema.getEntityDefinitionWithPluralNamed(term);
    }

    public EntityDefinition getEntityDefinitionNamed(final String term){
        return schema.getEntityDefinitionNamed(term);
    }

    // Instance Methods

    public List<EntityInstanceCollection> getAllEntityInstanceCollections() {
        return database.getAllInstanceCollections();
    }

    public EntityInstance findEntityInstanceByGuid(final String thingGUID) {
        return database.findEntityInstanceByGUID(thingGUID);
    }

    public EntityInstanceCollection getInstanceCollectionForEntityNamed(final String aName) {
        return database.getInstanceCollectionForEntityNamed(aName);
    }

    public void deleteEntityInstance(final EntityInstance anEntityInstance) {
        database.deleteEntityInstance(anEntityInstance);
    }

    public void clearAllData() {
        database.clearAllData();
    }

    public EntityRelModel cloneWithDifferentData(final List<EntityInstance> instances) {
        return new EntityRelModel(schema, new ERInstanceData(instances));
    }
}
